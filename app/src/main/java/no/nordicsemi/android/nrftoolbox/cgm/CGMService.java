package no.nordicsemi.android.nrftoolbox.cgm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.FeaturesActivity;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.ToolboxApplication;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.LoggableBleManager;

public class CGMService extends BleProfileService implements CGMManagerCallbacks {
    private static final String ACTION_DISCONNECT = "no.nordicsemi.android.nrftoolbox.cgms.ACTION_DISCONNECT";

    public static final String BROADCAST_BATTERY_LEVEL = "no.nordicsemi.android.nrftoolbox.BROADCAST_BATTERY_LEVEL";
    public static final String EXTRA_BATTERY_LEVEL = "no.nordicsemi.android.nrftoolbox.EXTRA_BATTERY_LEVEL";

    public static final String BROADCAST_NEW_CGMS_VALUE = "no.nordicsemi.android.nrftoolbox.cgms.BROADCAST_NEW_CGMS_VALUE";
    public static final String BROADCAST_DATA_SET_CLEAR = "no.nordicsemi.android.nrftoolbox.cgms.BROADCAST_DATA_SET_CLEAR";
    public static final String OPERATION_STARTED = "no.nordicsemi.android.nrftoolbox.cgms.OPERATION_STARTED";
    public static final String OPERATION_COMPLETED = "no.nordicsemi.android.nrftoolbox.cgms.OPERATION_COMPLETED";
    public static final String OPERATION_SUPPORTED = "no.nordicsemi.android.nrftoolbox.cgms.OPERATION_SUPPORTED";
    public static final String OPERATION_NOT_SUPPORTED = "no.nordicsemi.android.nrftoolbox.cgms.OPERATION_NOT_SUPPORTED";
    public static final String OPERATION_FAILED = "no.nordicsemi.android.nrftoolbox.cgms.OPERATION_FAILED";
    public static final String OPERATION_ABORTED = "no.nordicsemi.android.nrftoolbox.cgms.OPERATION_ABORTED";
    public static final String EXTRA_CGMS_RECORD = "no.nordicsemi.android.nrftoolbox.cgms.EXTRA_CGMS_RECORD";
    public static final String EXTRA_DATA = "no.nordicsemi.android.nrftoolbox.cgms.EXTRA_DATA";

    private final static int NOTIFICATION_ID = 229;
    private final static int OPEN_ACTIVITY_REQ = 0;
    private final static int DISCONNECT_REQ = 1;

    private CGMManager manager;
    private final LocalBinder binder = new CGMSBinder();

    /**
     * This local binder is an interface for the bonded activity to operate with the RSC sensor
     */

    class CGMSBinder extends LocalBinder {
        /**
         * Returns all records as a sparse array where sequence number is the key.
         *
         * @return the records list
         */
        SparseArray<CGMRecord> getRecords() {
            return manager.getRecords();
        }

        /**
         * Clears the records list locally
         */
        void clear() {
            if (manager != null)
                manager.clear();
        }

        /**
         * Sends the request to obtain the first (oldest) record from glucose device.
         * The data will be returned to Glucose Measurement characteristic as a notification followed by Record Access Control
         * Point indication with status code ({@link CGMManager# RESPONSE_SUCCESS} or other in case of error.
         */
        void getFirstRecord() {
            if (manager != null)
                manager.getFirstRecord();
        }

        /**
         * Sends the request to obtain the last (most recent) record from glucose device.
         * The data will be returned to Glucose Measurement characteristic as a notification followed by Record Access
         * Control Point indication with status code Success or other in case of error.
         */
        void getLastRecord() {
            if (manager != null)
                manager.getLastRecord();
        }

        /**
         * Sends the request to obtain all records from glucose device.
         * Initially we want to notify user about the number of the records so the Report Number of Stored Records is send.
         * The data will be returned to Glucose Measurement characteristic as a series of notifications followed
         * by Record Access Control Point indication with status code Success or other in case of error.
         */
        void getAllRecords() {
            if (manager != null)
                manager.getAllRecords();
        }

        /**
         * Sends the request to obtain all records from glucose device with sequence number greater
         * than the last one already obtained. The data will be returned to Glucose Measurement
         * characteristic as a series of notifications followed by Record Access Control Point
         * indication with status code Success or other in case of error.
         */
        void refreshRecords() {
            if (manager != null)
                manager.refreshRecords();
        }

        /**
         * Sends abort operation signal to the device
         */
        void abort() {
            if (manager != null)
                manager.abort();
        }

        /**
         * Sends Delete op code with All stored records parameter. This method may not be supported by the SDK sample.
         */
        void deleteAllRecords() {
            if (manager != null)
                manager.deleteAllRecords();
        }
    }

    @Override
    protected LocalBinder getBinder() {
        return binder;
    }

    @Override
    protected LoggableBleManager<CGMManagerCallbacks> initializeManager() {
        return manager = new CGMManager(this);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DISCONNECT);
        registerReceiver(disconnectActionBroadcastReceiver, filter);
    }

    @Override
    public void onDestroy() {
        // when user has disconnected from the sensor, we have to cancel the notification that we've created some milliseconds before using unbindService
        stopForegroundService();
        unregisterReceiver(disconnectActionBroadcastReceiver);

        super.onDestroy();
    }

    @Override
    protected void onRebind() {
        startForegroundService();
    }

    @Override
    protected void onUnbind() {
        startForegroundService();
    }

    /**
     * Sets the service as a foreground service
     */
    private void startForegroundService(){
        // when the activity closes we need to show the notification that user is connected to the peripheral sensor
        // We start the service as a foreground service as Android 8.0 (Oreo) onwards kills any running background services
        final Notification notification = createNotification(R.string.uart_notification_connected_message, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, notification);
        } else {
            final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(NOTIFICATION_ID, notification);
        }
    }

    /**
     * Stops the service as a foreground service
     */
    private void stopForegroundService(){
        // when the activity rebinds to the service, remove the notification and stop the foreground service
        // on devices running Android 8.0 (Oreo) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        } else {
            cancelNotification();
        }
    }

    /**
     * Creates the notification
     *
     * @param messageResId the message resource id. The message must have one String parameter,<br />
     *                     f.e. <code>&lt;string name="name"&gt;%s is connected&lt;/string&gt;</code>
     * @param defaults     signals that will be used to notify the user
     */
    @SuppressWarnings("SameParameterValue")
    private Notification createNotification(final int messageResId, final int defaults) {
        final Intent parentIntent = new Intent(this, FeaturesActivity.class);
        parentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final Intent targetIntent = new Intent(this, CGMSActivity.class);

        final Intent disconnect = new Intent(ACTION_DISCONNECT);
        final PendingIntent disconnectAction = PendingIntent.getBroadcast(this, DISCONNECT_REQ, disconnect, PendingIntent.FLAG_UPDATE_CURRENT);

        // both activities above have launchMode="singleTask" in the AndroidManifest.xml file, so if the task is already running, it will be resumed
        final PendingIntent pendingIntent = PendingIntent.getActivities(this, OPEN_ACTIVITY_REQ, new Intent[]{parentIntent, targetIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ToolboxApplication.CONNECTED_DEVICE_CHANNEL);
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(getString(R.string.app_name)).setContentText(getString(messageResId, getDeviceName()));
        builder.setSmallIcon(R.drawable.ic_stat_notify_cgms);
        builder.setShowWhen(defaults != 0).setDefaults(defaults).setAutoCancel(true).setOngoing(true);
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_action_bluetooth, getString(R.string.csc_notification_action_disconnect), disconnectAction));

        return builder.build();
    }

    /**
     * Cancels the existing notification. If there is no active notification this method does nothing
     */
    private void cancelNotification() {
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);
    }

    /**
     * This broadcast receiver listens for {@link #ACTION_DISCONNECT} that may be fired by pressing Disconnect action button on the notification.
     */
    private final BroadcastReceiver disconnectActionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Logger.i(getLogSession(), "[Notification] Disconnect action pressed");
            if (isConnected())
                getBinder().disconnect();
            else
                stopSelf();
        }
    };

    @Override
    public void onCGMValueReceived(@NonNull final BluetoothDevice device, @NonNull final CGMRecord record) {
        final Intent broadcast = new Intent(BROADCAST_NEW_CGMS_VALUE);
        broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
        broadcast.putExtra(EXTRA_CGMS_RECORD, record);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onOperationStarted(@NonNull final BluetoothDevice device) {
        final Intent broadcast = new Intent(OPERATION_STARTED);
        broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
        broadcast.putExtra(EXTRA_DATA, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onOperationCompleted(@NonNull final BluetoothDevice device) {
        final Intent broadcast = new Intent(OPERATION_COMPLETED);
        broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
        broadcast.putExtra(EXTRA_DATA, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onOperationFailed(@NonNull final BluetoothDevice device) {
        final Intent broadcast = new Intent(OPERATION_FAILED);
        broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
        broadcast.putExtra(EXTRA_DATA, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onOperationAborted(@NonNull final BluetoothDevice device) {
        final Intent broadcast = new Intent(OPERATION_ABORTED);
        broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
        broadcast.putExtra(EXTRA_DATA, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onOperationNotSupported(@NonNull final BluetoothDevice device) {
        final Intent broadcast = new Intent(OPERATION_NOT_SUPPORTED);
        broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
        broadcast.putExtra(EXTRA_DATA, false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onDataSetCleared(@NonNull final BluetoothDevice device) {
        final Intent broadcast = new Intent(BROADCAST_DATA_SET_CLEAR);
        broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onNumberOfRecordsRequested(@NonNull final BluetoothDevice device, final int value) {
        if (value == 0)
            showToast(R.string.gls_progress_zero);
        else
            showToast(getResources().getQuantityString(R.plurals.gls_progress, value, value));

    }

    @Override
    public void onBatteryLevelChanged(@NonNull final BluetoothDevice device, final int batteryLevel) {
        final Intent broadcast = new Intent(BROADCAST_BATTERY_LEVEL);
        broadcast.putExtra(EXTRA_DEVICE, device);
        broadcast.putExtra(EXTRA_BATTERY_LEVEL, batteryLevel);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }
}
