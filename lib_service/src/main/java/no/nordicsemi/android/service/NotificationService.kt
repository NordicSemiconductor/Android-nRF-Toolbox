/*
 * Copyright (c) 2022, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService

private const val CHANNEL_ID = "FOREGROUND_BLE_SERVICE"

abstract class NotificationService : LifecycleService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        startForegroundService()
        return result
    }

    override fun onDestroy() {
        // when user has disconnected from the sensor, we have to cancel the notification that we've created some milliseconds before using unbindService
        cancelNotification()
        stopForegroundService()
        super.onDestroy()
    }

    /**
     * Sets the service as a foreground service
     */
    private fun startForegroundService() {
        // when the activity closes we need to show the notification that user is connected to the peripheral sensor
        // We start the service as a foreground service as Android 8.0 (Oreo) onwards kills any running background services
        val notification = createNotification(R.string.csc_notification_connected_message)
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
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            cancelNotification()
        }
    }

    /**
     * Creates the notification
     *
     * @param messageResId the message resource id. The message must have one String parameter,<br></br>
     * f.e. `<string name="name">%s is connected</string>`
     */
    private fun createNotification(messageResId: Int): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(CHANNEL_ID)
        }

        val intent: Intent? = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(messageResId, "Device"))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setColor(ContextCompat.getColor(this, R.color.md_theme_primary))
            .setContentIntent(pendingIntent)
            .build()
    }

    @Suppress("SameParameterValue")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelName: String) {
        val channel = NotificationChannel(
            channelName,
            getString(R.string.channel_connected_devices_title),
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = getString(R.string.channel_connected_devices_description)
        channel.setShowBadge(false)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
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
