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
package no.nordicsemi.android.service

import android.app.Notification
import android.app.NotificationManager
import android.os.Build

abstract class ForegroundBleService<T : BatteryManager<out BatteryManagerCallbacks>> : BleProfileService() {

    protected abstract val manager: T

    override fun onDestroy() {
        // when user has disconnected from the sensor, we have to cancel the notification that we've created some milliseconds before using unbindService
        cancelNotification()
        super.onDestroy()
    }

    override fun onRebind() {
        stopForegroundService()
        if (isConnected) {
            // This method will read the Battery Level value, if possible and then try to enable battery notifications (if it has NOTIFY property).
            // If the Battery Level characteristic has only the NOTIFY property, it will only try to enable notifications.
            manager.readBatteryLevelCharacteristic()
        }
    }

    override fun onUnbind() {
        // When we are connected, but the application is not open, we are not really interested in battery level notifications.
        // But we will still be receiving other values, if enabled.
        if (isConnected) manager.disableBatteryLevelCharacteristicNotifications()
        startForegroundService()
    }

    /**
     * Sets the service as a foreground service
     */
    private fun startForegroundService() {
        // when the activity closes we need to show the notification that user is connected to the peripheral sensor
        // We start the service as a foreground service as Android 8.0 (Oreo) onwards kills any running background services
        val notification = createNotification(R.string.csc_notification_connected_message, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, notification)
        } else {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIFICATION_ID, notification)
        }
    }

    /**
     * Stops the service as a foreground service
     */
    private fun stopForegroundService() {
        // when the activity rebinds to the service, remove the notification and stop the foreground service
        // on devices running Android 8.0 (Oreo) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true)
        } else {
            cancelNotification()
        }
    }

    /**
     * Creates the notification
     *
     * @param messageResId the message resource id. The message must have one String parameter,<br></br>
     * f.e. `<string name="name">%s is connected</string>`
     * @param defaults
     */
    private fun createNotification(messageResId: Int, defaults: Int): Notification {
        TODO()
//        final Intent parentIntent = new Intent(this, FeaturesActivity.class);
//        parentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        final Intent targetIntent = new Intent(this, CSCActivity.class);
//
//        final Intent disconnect = new Intent(ACTION_DISCONNECT);
//        final PendingIntent disconnectAction = PendingIntent.getBroadcast(this, DISCONNECT_REQ, disconnect, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        // both activities above have launchMode="singleTask" in the AndroidManifest.xml file, so if the task is already running, it will be resumed
//        final PendingIntent pendingIntent = PendingIntent.getActivities(this, OPEN_ACTIVITY_REQ, new Intent[]{parentIntent, targetIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
//        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ToolboxApplication.CONNECTED_DEVICE_CHANNEL);
//        builder.setContentIntent(pendingIntent);
//        builder.setContentTitle(getString(R.string.app_name)).setContentText(getString(messageResId, getDeviceName()));
//        builder.setSmallIcon(R.drawable.ic_stat_notify_csc);
//        builder.setShowWhen(defaults != 0).setDefaults(defaults).setAutoCancel(true).setOngoing(true);
//        builder.addAction(new NotificationCompat.Action(R.drawable.ic_action_bluetooth, getString(R.string.csc_notification_action_disconnect), disconnectAction));
//
//        return builder.build();
    }

    /**
     * Cancels the existing notification. If there is no active notification this method does nothing
     */
    private fun cancelNotification() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIFICATION_ID)
    }

    companion object {
        private const val NOTIFICATION_ID = 200
    }
}
