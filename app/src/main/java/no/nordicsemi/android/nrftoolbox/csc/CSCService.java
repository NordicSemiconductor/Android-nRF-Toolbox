/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.nrftoolbox.csc;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.FeaturesActivity;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.ToolboxApplication;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.LoggableBleManager;

public class CSCService extends BleProfileService implements CSCManagerCallbacks {
    @SuppressWarnings("unused")
    private static final String TAG = "CSCService";

    public static final String BROADCAST_WHEEL_DATA = "no.nordicsemi.android.nrftoolbox.csc.BROADCAST_WHEEL_DATA";
    /**
     * Speed in meters per second.
     */
    public static final String EXTRA_SPEED = "no.nordicsemi.android.nrftoolbox.csc.EXTRA_SPEED";
    /**
     * Distance in meters.
     */
    public static final String EXTRA_DISTANCE = "no.nordicsemi.android.nrftoolbox.csc.EXTRA_DISTANCE";
    /**
     * Total distance in meters.
     */
    public static final String EXTRA_TOTAL_DISTANCE = "no.nordicsemi.android.nrftoolbox.csc.EXTRA_TOTAL_DISTANCE";

    public static final String BROADCAST_CRANK_DATA = "no.nordicsemi.android.nrftoolbox.csc.BROADCAST_CRANK_DATA";
    public static final String EXTRA_GEAR_RATIO = "no.nordicsemi.android.nrftoolbox.csc.EXTRA_GEAR_RATIO";
    public static final String EXTRA_CADENCE = "no.nordicsemi.android.nrftoolbox.csc.EXTRA_CADENCE";

    public static final String BROADCAST_BATTERY_LEVEL = "no.nordicsemi.android.nrftoolbox.BROADCAST_BATTERY_LEVEL";
    public static final String EXTRA_BATTERY_LEVEL = "no.nordicsemi.android.nrftoolbox.EXTRA_BATTERY_LEVEL";

    private static final String ACTION_DISCONNECT = "no.nordicsemi.android.nrftoolbox.csc.ACTION_DISCONNECT";

    private final static int NOTIFICATION_ID = 200;
    private final static int OPEN_ACTIVITY_REQ = 0;
    private final static int DISCONNECT_REQ = 1;

    private final LocalBinder binder = new CSCBinder();
    private CSCManager manager;

    /**
     * This local binder is an interface for the bonded activity to operate with the RSC sensor
     */
    class CSCBinder extends LocalBinder {
        // empty
    }

    @Override
    protected LocalBinder getBinder() {
        return binder;
    }

    @Override
    protected LoggableBleManager<CSCManagerCallbacks> initializeManager() {
        return manager = new CSCManager(this);
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
        cancelNotification();
        unregisterReceiver(disconnectActionBroadcastReceiver);

        super.onDestroy();
    }

    @Override
    protected void onRebind() {
        stopForegroundService();

        if (isConnected()) {
            // This method will read the Battery Level value, if possible and then try to enable battery notifications (if it has NOTIFY property).
            // If the Battery Level characteristic has only the NOTIFY property, it will only try to enable notifications.
            manager.readBatteryLevelCharacteristic();
        }
    }

    @Override
    protected void onUnbind() {
        // When we are connected, but the application is not open, we are not really interested in battery level notifications.
        // But we will still be receiving other values, if enabled.
        if (isConnected())
            manager.disableBatteryLevelCharacteristicNotifications();
        startForegroundService();
    }

    @Override
    public void onDistanceChanged(@NonNull final BluetoothDevice device, final float totalDistance, final float distance, final float speed) {
        final Intent broadcast = new Intent(BROADCAST_WHEEL_DATA);
        broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
        broadcast.putExtra(EXTRA_SPEED, speed);
        broadcast.putExtra(EXTRA_DISTANCE, distance);
        broadcast.putExtra(EXTRA_TOTAL_DISTANCE, totalDistance);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onCrankDataChanged(@NonNull final BluetoothDevice device, final float crankCadence, final float gearRatio) {
        final Intent broadcast = new Intent(BROADCAST_CRANK_DATA);
        broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
        broadcast.putExtra(EXTRA_GEAR_RATIO, gearRatio);
        broadcast.putExtra(EXTRA_CADENCE, (int) crankCadence);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onBatteryLevelChanged(@NonNull final BluetoothDevice device, final int batteryLevel) {
        final Intent broadcast = new Intent(BROADCAST_BATTERY_LEVEL);
        broadcast.putExtra(EXTRA_DEVICE, getBluetoothDevice());
        broadcast.putExtra(EXTRA_BATTERY_LEVEL, batteryLevel);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    /**
     * Sets the service as a foreground service
     */
    private void startForegroundService(){
        // when the activity closes we need to show the notification that user is connected to the peripheral sensor
        // We start the service as a foreground service as Android 8.0 (Oreo) onwards kills any running background services
        final Notification notification = createNotification(R.string.csc_notification_connected_message, 0);
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
     * @param defaults
     */
    @SuppressWarnings("SameParameterValue")
    private Notification createNotification(final int messageResId, final int defaults) {
        final Intent parentIntent = new Intent(this, FeaturesActivity.class);
        parentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final Intent targetIntent = new Intent(this, CSCActivity.class);

        final Intent disconnect = new Intent(ACTION_DISCONNECT);
        final PendingIntent disconnectAction = PendingIntent.getBroadcast(this, DISCONNECT_REQ, disconnect, PendingIntent.FLAG_UPDATE_CURRENT);

        // both activities above have launchMode="singleTask" in the AndroidManifest.xml file, so if the task is already running, it will be resumed
        final PendingIntent pendingIntent = PendingIntent.getActivities(this, OPEN_ACTIVITY_REQ, new Intent[]{parentIntent, targetIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ToolboxApplication.CONNECTED_DEVICE_CHANNEL);
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(getString(R.string.app_name)).setContentText(getString(messageResId, getDeviceName()));
        builder.setSmallIcon(R.drawable.ic_stat_notify_csc);
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
}
