package no.nordicsemi.android.nrftoolbox.cgms;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.FeaturesActivity;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.ToolboxApplication;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;

public class CGMService extends BleProfileService implements CGMSManagerCallbacks {
    private static final String ACTION_DISCONNECT = "no.nordicsemi.android.nrftoolbox.cgms.ACTION_DISCONNECT";
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

    private CGMSManager mManager;
    private final LocalBinder mBinder = new CGMSBinder();

    /**
     * This local binder is an interface for the bonded activity to operate with the RSC sensor
     */

    public class CGMSBinder extends LocalBinder {
        /**
         * Returns all records as a sparse array where sequence number is the key.
         *
         * @return the records list
         */
        public SparseArray<CGMSRecord> getRecords() {
            return mManager.getRecords();
        }

        /**
         * Clears the records list locally
         */
        public void clear() {
            if (mManager != null)
                mManager.clear();
        }

        /**
         * Sends the request to obtain the first (oldest) record from glucose device.
         * The data will be returned to Glucose Measurement characteristic as a notification followed by Record Access Control
         * Point indication with status code ({@link CGMSManager# RESPONSE_SUCCESS} or other in case of error.
         */
        public void getFirstRecord() {
            if(mManager != null)
                mManager.getFirstRecord();
        }

        /**
         * Sends the request to obtain the last (most recent) record from glucose device.
         * The data will be returned to Glucose Measurement characteristic as a notification followed by Record Access
         * Control Point indication with status code ({@link CGMSManager#RESPONSE_SUCCESS} or other in case of error.
         */
        public void getLastRecord() {
            if(mManager != null)
                mManager.getLastRecord();
        }

        /**
         * Sends the request to obtain all records from glucose device.
         * Initially we want to notify user about the number of the records so the {@link CGMSManager#OP_CODE_REPORT_NUMBER_OF_RECORDS} is send.
         * The data will be returned to Glucose Measurement characteristic as a series of notifications followed by Record Access Control Point
         * indication with status code ({@link CGMSManager#RESPONSE_SUCCESS} or other in case of error.
         */
        public void getAllRecords() {
            if(mManager != null)
                mManager.getAllRecords();
        }

		/**
         * Sends the request to obtain all records from glucose device with sequence number greater than the last one already obtained.
         * The data will be returned to Glucose Measurement characteristic as a series of notifications followed by Record Access Control Point
         * indication with status code ({@link CGMSManager#RESPONSE_SUCCESS} or other in case of error.
         */
        public void refreshRecords() {
            if (mManager != null)
                mManager.refreshRecords();
        }

        /**
         * Sends abort operation signal to the device
         */
        public void abort() {
            if(mManager != null)
                mManager.abort();
        }

		/**
         * Sends Delete op code with All stored records parameter. This method may not be supported by the SDK sample.
         */
        public void deleteAllRecords() {
            if(mManager != null)
                mManager.deleteAllRecords();
        }
    }

    @Override
    protected LocalBinder getBinder() {
        return mBinder;
    }

    @Override
    protected BleManager initializeManager() {
        return mManager = new CGMSManager(this);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DISCONNECT);
        registerReceiver(mDisconnectActionBroadcastReceiver, filter);
    }

    @Override
    public void onDestroy() {
        // when user has disconnected from the sensor, we have to cancel the notification that we've created some milliseconds before using unbindService
        cancelNotification();
        unregisterReceiver(mDisconnectActionBroadcastReceiver);

        super.onDestroy();
    }

    @Override
    protected void onRebind() {
        // when the activity rebinds to the service, remove the notification
        cancelNotification();
    }

    @Override
    protected void onUnbind() {
        // when the activity closes we need to show the notification that user is connected to the sensor
        createNotification(R.string.csc_notification_connected_message, 0);
    }

    /**
     * Creates the notification
     *
     * @param messageResId
     *            the message resource id. The message must have one String parameter,<br />
     *            f.e. <code>&lt;string name="name"&gt;%s is connected&lt;/string&gt;</code>
     * @param defaults
     *            signals that will be used to notify the user
     */
    private void createNotification(final int messageResId, final int defaults) {
        final Intent parentIntent = new Intent(this, FeaturesActivity.class);
        parentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final Intent targetIntent = new Intent(this, CGMSActivity.class);

        final Intent disconnect = new Intent(ACTION_DISCONNECT);
        final PendingIntent disconnectAction = PendingIntent.getBroadcast(this, DISCONNECT_REQ, disconnect, PendingIntent.FLAG_UPDATE_CURRENT);

        // both activities above have launchMode="singleTask" in the AndroidManifest.xml file, so if the task is already running, it will be resumed
        final PendingIntent pendingIntent = PendingIntent.getActivities(this, OPEN_ACTIVITY_REQ, new Intent[] { parentIntent, targetIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ToolboxApplication.CONNECTED_DEVICE_CHANNEL);
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(getString(R.string.app_name)).setContentText(getString(messageResId, getDeviceName()));
        builder.setSmallIcon(R.drawable.ic_stat_notify_cgms);
        builder.setShowWhen(defaults != 0).setDefaults(defaults).setAutoCancel(true).setOngoing(true);
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_action_bluetooth, getString(R.string.csc_notification_action_disconnect), disconnectAction));

        final Notification notification = builder.build();
        final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, notification);
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
    private final BroadcastReceiver mDisconnectActionBroadcastReceiver = new BroadcastReceiver() {
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
    public void onCGMValueReceived(final BluetoothDevice device, final CGMSRecord record) {
        final Intent broadcast = new Intent(BROADCAST_NEW_CGMS_VALUE);
        broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
        broadcast.putExtra(EXTRA_CGMS_RECORD, record);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onOperationStarted(final BluetoothDevice device) {
        final Intent broadcast = new Intent(OPERATION_STARTED);
        broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
        broadcast.putExtra(EXTRA_DATA, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onOperationCompleted(final BluetoothDevice device) {
        final Intent broadcast = new Intent(OPERATION_COMPLETED);
        broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
        broadcast.putExtra(EXTRA_DATA, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onOperationFailed(final BluetoothDevice device) {
        final Intent broadcast = new Intent(OPERATION_FAILED);
        broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
        broadcast.putExtra(EXTRA_DATA, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onOperationAborted(final BluetoothDevice device) {
        final Intent broadcast = new Intent(OPERATION_ABORTED);
        broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
        broadcast.putExtra(EXTRA_DATA, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onOperationNotSupported(final BluetoothDevice device) {
        final Intent broadcast = new Intent(OPERATION_NOT_SUPPORTED);
        broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
        broadcast.putExtra(EXTRA_DATA, false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onDatasetClear(final BluetoothDevice device) {
        final Intent broadcast = new Intent(BROADCAST_DATA_SET_CLEAR);
        broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onNumberOfRecordsRequested(final BluetoothDevice device, int value) {
        showToast(getResources().getQuantityString(R.plurals.gls_progress, value, value));
    }
}
