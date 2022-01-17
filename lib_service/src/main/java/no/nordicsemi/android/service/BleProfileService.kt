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

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.log.ILogSession
import no.nordicsemi.android.log.Logger
import javax.inject.Inject

@AndroidEntryPoint
abstract class BleProfileService : Service() {

    protected val scope = CloseableCoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    protected abstract val manager: BleManager

    private val _status = MutableStateFlow(BleManagerStatus.CONNECTING)
    val status = _status.asStateFlow()

    @Inject
    lateinit var bluetoothDeviceHolder: SelectedBluetoothDeviceHolder

    /**
     * Returns a handler that is created in onCreate().
     * The handler may be used to postpone execution of some operations or to run them in UI thread.
     */
    private var handler: Handler? = null

    private var activityIsChangingConfiguration = false

    /**
     * Returns the Bluetooth device object
     *
     * @return bluetooth device
     */
    private val bluetoothDevice: BluetoothDevice by lazy {
        bluetoothDeviceHolder.device?.device ?: throw IllegalArgumentException(
            "No device associated with the application."
        )
    }

    /**
     * Returns the log session that can be used to append log entries. The method returns `null` if the nRF Logger app was not installed. It is safe to use logger when
     * [.onServiceStarted] has been called.
     *
     * @return the log session
     */
    private var logSession: ILogSession? = null
        private set

    override fun onCreate() {
        super.onCreate()
        handler = Handler()

        manager.setConnectionObserver(object : ConnectionObserverAdapter() {
            override fun onDeviceConnected(device: BluetoothDevice) {
                super.onDeviceConnected(device)
                _status.value = BleManagerStatus.OK
            }

            override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
                super.onDeviceDisconnected(device, reason)
                _status.value = BleManagerStatus.DISCONNECTED
                scope.close()
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * This method returns whether autoConnect option should be used.
     *
     * @return true to use autoConnect feature, false (default) otherwise.
     */
    private fun shouldAutoConnect(): Boolean {
        return false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        manager.connect(bluetoothDevice)
            .useAutoConnect(shouldAutoConnect())
            .retry(3, 100)
            .enqueue()

        return START_REDELIVER_INTENT
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        // This method is called when user removed the app from Recents.
        // By default, the service will be killed and recreated immediately after that.
        // However, all managed devices will be lost and devices will be disconnected.
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()

        // shutdown the manager
        manager.disconnect().enqueue()
        Logger.i(logSession, "Service destroyed")
        bluetoothDeviceHolder.forgetDevice()
        logSession = null
        handler = null
    }

    /**
     * This method should return false if the service needs to do some asynchronous work after if has disconnected from the device.
     * In that case the [.stopService] method must be called when done.
     *
     * @return true (default) to automatically stop the service when device is disconnected. False otherwise.
     */
    protected fun stopWhenDisconnected(): Boolean {
        return true
    }

    private fun stopService() {
        // user requested disconnection. We must stop the service
        Logger.v(logSession, "Stopping service...")
        stopSelf()
    }

    /**
     * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
     *
     * @param messageResId an resource id of the message to be shown
     */
    protected fun showToast(messageResId: Int) {
        handler?.post {
            Toast.makeText(this@BleProfileService, messageResId, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
     *
     * @param message a message to be shown
     */
    protected fun showToast(message: String?) {
        handler?.post {
            Toast.makeText(this@BleProfileService, message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Returns the device address
     *
     * @return device address
     */
    protected val deviceAddress: String
        get() = bluetoothDevice.address

    /**
     * Returns `true` if the device is connected to the sensor.
     *
     * @return `true` if device is connected to the sensor, `false` otherwise
     */
    protected val isConnected: Boolean
        get() = manager.isConnected
}
