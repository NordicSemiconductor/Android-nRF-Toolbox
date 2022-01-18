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
package no.nordicsemi.dfu.repository

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.dfu.DfuBaseService
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.android.service.CloseableCoroutineScope
import no.nordicsemi.dfu.R
import no.nordicsemi.dfu.data.DFURepository
import javax.inject.Inject

@AndroidEntryPoint
internal class DFUService : DfuBaseService() {

    private val scope = CloseableCoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Inject
    lateinit var repository: DFURepository

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createDfuNotificationChannel(this)
        }

        repository.command.onEach {
            stopSelf()
        }.launchIn(scope)

        repository.setNewStatus(BleManagerStatus.OK)
    }

    override fun getNotificationTarget(): Class<out Activity?>? {
        /*
		 * As a target activity the NotificationActivity is returned, not the MainActivity. This is because the notification must create a new task:
		 * 
		 * intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		 * 
		 * when user press it. Using NotificationActivity we can check whether the new activity is a root activity (that means no other activity was open before)
		 * or that there is other activity already open. In the later case the notificationActivity will just be closed. System will restore the previous activity.
		 * However if the application has been closed during upload and user click the notification a NotificationActivity will be launched as a root activity.
		 * It will create and start the main activity and terminate itself.
		 * 
		 * This method may be used to restore the target activity in case the application was closed or is open. It may also be used to recreate an activity
		 * history (see NotificationActivity).
		 */
        return Class.forName("no.nordicsemi.android.nrftoolbox.MainActivity") as Class<out Activity>
    }

    override fun isDebug(): Boolean {
        // return BuildConfig.DEBUG;
        return true
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createDfuNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_DFU,
            context.getString(R.string.dfu_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = context.getString(R.string.dfu_channel_description)
        channel.setShowBadge(false)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager?.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        repository.setNewStatus(BleManagerStatus.DISCONNECTED)
        super.onDestroy()
        scope.close()
    }
}
