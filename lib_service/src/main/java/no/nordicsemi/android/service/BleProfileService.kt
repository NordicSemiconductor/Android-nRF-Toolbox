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

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.ble.BleManagerCallbacks
import no.nordicsemi.android.ble.utils.ILogger
import no.nordicsemi.android.log.ILogSession
import no.nordicsemi.android.log.Logger
import no.nordicsemi.android.scanner.tools.SelectedBluetoothDeviceHolder
import javax.inject.Inject

@AndroidEntryPoint
abstract class BleProfileService : LifecycleService(), BleManagerCallbacks {

    private var bleManager: LoggableBleManager<out BleManagerCallbacks>? = null

    @Inject
    lateinit var bluetoothDeviceHolder: SelectedBluetoothDeviceHolder

    /**
     * Returns a handler that is created in onCreate().
     * The handler may be used to postpone execution of some operations or to run them in UI thread.
     */
    protected var handler: Handler? = null
        private set
    protected var bound = false
    private var activityIsChangingConfiguration = false

    /**
     * Returns the Bluetooth device object
     *
     * @return bluetooth device
     */
    protected val bluetoothDevice: BluetoothDevice by lazy {
        bluetoothDeviceHolder.device ?: throw UnsupportedOperationException(
            "No device address at EXTRA_DEVICE_ADDRESS key"
        )
    }

    /**
     * Returns the device name
     *
     * @return the device name
     */
    protected var deviceName: String? = null
        private set

    /**
     * Returns the log session that can be used to append log entries. The method returns `null` if the nRF Logger app was not installed. It is safe to use logger when
     * [.onServiceStarted] has been called.
     *
     * @return the log session
     */
    protected var logSession: ILogSession? = null
        private set
    private val bluetoothStateBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)
            val logger: ILogger = binder
            val stateString =
                "[Broadcast] Action received: " + BluetoothAdapter.ACTION_STATE_CHANGED + ", state changed to " + state2String(
                    state
                )
            logger.log(Log.DEBUG, stateString)
            when (state) {
                BluetoothAdapter.STATE_ON -> onBluetoothEnabled()
                BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_OFF -> onBluetoothDisabled()
            }
        }

        private fun state2String(state: Int): String {
            return when (state) {
                BluetoothAdapter.STATE_TURNING_ON -> "TURNING ON"
                BluetoothAdapter.STATE_ON -> "ON"
                BluetoothAdapter.STATE_TURNING_OFF -> "TURNING OFF"
                BluetoothAdapter.STATE_OFF -> "OFF"
                else -> "UNKNOWN ($state)"
            }
        }
    }

    inner class LocalBinder : Binder(), ILogger {
        /**
         * Disconnects from the sensor.
         */
        fun disconnect() {
            val state = bleManager!!.connectionState
            if (state == BluetoothGatt.STATE_DISCONNECTED || state == BluetoothGatt.STATE_DISCONNECTING) {
                bleManager!!.close()
                onDeviceDisconnected(bluetoothDevice!!)
                return
            }
            bleManager!!.disconnect().enqueue()
        }

        /**
         * Sets whether the bound activity if changing configuration or not.
         * If `false`, we will turn off battery level notifications in onUnbind(..) method below.
         *
         * @param changing true if the bound activity is finishing
         */
        fun setActivityIsChangingConfiguration(changing: Boolean) {
            activityIsChangingConfiguration = changing
        }

        /**
         * Returns the device address
         *
         * @return device address
         */
        val deviceAddress: String
            get() = bluetoothDevice!!.address

        /**
         * Returns the device name
         *
         * @return the device name
         */
        fun getDeviceName(): String? {
            return deviceName
        }

        /**
         * Returns the Bluetooth device
         *
         * @return the Bluetooth device
         */
        fun getBluetoothDevice(): BluetoothDevice? {
            return bluetoothDevice
        }

        /**
         * Returns `true` if the device is connected to the sensor.
         *
         * @return `true` if device is connected to the sensor, `false` otherwise
         */
        val isConnected: Boolean
            get() = bleManager!!.isConnected

        /**
         * Returns the connection state of given device.
         *
         * @return the connection state, as in [BleManager.getConnectionState].
         */
        val connectionState: Int
            get() = bleManager!!.connectionState

        /**
         * Returns the log session that can be used to append log entries.
         * The log session is created when the service is being created.
         * The method returns `null` if the nRF Logger app was not installed.
         *
         * @return the log session
         */
        fun getLogSession(): ILogSession? {
            return logSession
        }

        override fun log(level: Int, message: String) {
            Logger.log(logSession, level, message)
        }

        override fun log(level: Int, @StringRes messageRes: Int, vararg params: Any) {
            Logger.log(logSession, level, messageRes, *params)
        }
    }// default implementation returns the basic binder. You can overwrite the LocalBinder with your own, wider implementation

    /**
     * Returns the binder implementation. This must return class implementing the additional manager interface that may be used in the bound activity.
     *
     * @return the service binder
     */
    protected val binder: LocalBinder
        protected get() =// default implementation returns the basic binder. You can overwrite the LocalBinder with your own, wider implementation
            LocalBinder()

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        bound = true
        return binder
    }

    override fun onRebind(intent: Intent) {
        bound = true
        if (!activityIsChangingConfiguration) onRebind()
    }

    /**
     * Called when the activity has rebound to the service after being recreated.
     * This method is not called when the activity was killed to be recreated when the phone orientation changed
     * if prior to being killed called [LocalBinder.setActivityIsChangingConfiguration] with parameter true.
     */
    protected open fun onRebind() {
        // empty default implementation
    }

    override fun onUnbind(intent: Intent): Boolean {
        bound = false
        if (!activityIsChangingConfiguration) onUnbind()

        // We want the onRebind method be called if anything else binds to it again
        return true
    }

    /**
     * Called when the activity has unbound from the service before being finished.
     * This method is not called when the activity is killed to be recreated when the phone orientation changed.
     */
    protected open fun onUnbind() {
        // empty default implementation
    }

    override fun onCreate() {
        super.onCreate()
        handler = Handler()

        // Initialize the manager
        bleManager = initializeManager()

        // Register broadcast receivers
        registerReceiver(
            bluetoothStateBroadcastReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )

        // Service has now been created
        onServiceCreated()

        // Call onBluetoothEnabled if Bluetooth enabled
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter.isEnabled) {
            onBluetoothEnabled()
        }
    }

    /**
     * Called when the service has been created, before the [.onBluetoothEnabled] is called.
     */
    protected fun onServiceCreated() {
        // empty default implementation
    }

    /**
     * Initializes the Ble Manager responsible for connecting to a single device.
     *
     * @return a new BleManager object
     */
    protected abstract fun initializeManager(): LoggableBleManager<out BleManagerCallbacks>

    /**
     * This method returns whether autoConnect option should be used.
     *
     * @return true to use autoConnect feature, false (default) otherwise.
     */
    protected fun shouldAutoConnect(): Boolean {
        return false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val logUri = intent?.getParcelableExtra<Uri>(EXTRA_LOG_URI)
        logSession = Logger.openSession(applicationContext, logUri)
        deviceName = intent?.getStringExtra(EXTRA_DEVICE_NAME)
        Logger.i(logSession, "Service started")
        val adapter = BluetoothAdapter.getDefaultAdapter()
        bleManager!!.setLogger(logSession)
        onServiceStarted()
        bleManager!!.connect(bluetoothDevice)
            .useAutoConnect(shouldAutoConnect())
            .retry(3, 100)
            .enqueue()
        return START_REDELIVER_INTENT
    }

    /**
     * Called when the service has been started. The device name and address are set.
     * The BLE Manager will try to connect to the device after this method finishes.
     */
    protected fun onServiceStarted() {
        // empty default implementation
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
        // Unregister broadcast receivers
        unregisterReceiver(bluetoothStateBroadcastReceiver)

        // shutdown the manager
        bleManager!!.close()
        Logger.i(logSession, "Service destroyed")
        bleManager = null
        bluetoothDeviceHolder.forgetDevice()
        deviceName = null
        logSession = null
        handler = null
    }

    /**
     * Method called when Bluetooth Adapter has been disabled.
     */
    protected fun onBluetoothDisabled() {
        // empty default implementation
    }

    /**
     * This method is called when Bluetooth Adapter has been enabled and
     * after the service was created if Bluetooth Adapter was enabled at that moment.
     * This method could initialize all Bluetooth related features, for example open the GATT server.
     */
    protected fun onBluetoothEnabled() {
        // empty default implementation
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        val broadcast = Intent(BROADCAST_CONNECTION_STATE)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_CONNECTING)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        val broadcast = Intent(BROADCAST_CONNECTION_STATE)
        broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_CONNECTED)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_DEVICE_NAME, deviceName)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        // Notify user about changing the state to DISCONNECTING
        val broadcast = Intent(BROADCAST_CONNECTION_STATE)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_DISCONNECTING)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
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

    override fun onDeviceDisconnected(device: BluetoothDevice) {
        // Note 1: Do not use the device argument here unless you change calling onDeviceDisconnected from the binder above

        // Note 2: if BleManager#shouldAutoConnect() for this device returned true, this callback will be
        // invoked ONLY when user requested disconnection (using Disconnect button). If the device
        // disconnects due to a link loss, the onLinkLossOccurred(BluetoothDevice) method will be called instead.
        val broadcast = Intent(BROADCAST_CONNECTION_STATE)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_DISCONNECTED)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
        if (stopWhenDisconnected()) stopService()
    }

    protected fun stopService() {
        // user requested disconnection. We must stop the service
        Logger.v(logSession, "Stopping service...")
        stopSelf()
    }

    override fun onLinkLossOccurred(device: BluetoothDevice) {
        val broadcast = Intent(BROADCAST_CONNECTION_STATE)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_LINK_LOSS)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }

    override fun onServicesDiscovered(device: BluetoothDevice, optionalServicesFound: Boolean) {
        val broadcast = Intent(BROADCAST_SERVICES_DISCOVERED)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_SERVICE_PRIMARY, true)
        broadcast.putExtra(EXTRA_SERVICE_SECONDARY, optionalServicesFound)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        val broadcast = Intent(BROADCAST_DEVICE_READY)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }

    override fun onDeviceNotSupported(device: BluetoothDevice) {
        val broadcast = Intent(BROADCAST_SERVICES_DISCOVERED)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_SERVICE_PRIMARY, false)
        broadcast.putExtra(EXTRA_SERVICE_SECONDARY, false)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)

        // no need for disconnecting, it will be disconnected by the manager automatically
    }

    override fun onBatteryValueReceived(device: BluetoothDevice, value: Int) {
        val broadcast = Intent(BROADCAST_BATTERY_LEVEL)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_BATTERY_LEVEL, value)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }

    override fun onBondingRequired(device: BluetoothDevice) {
        showToast(R.string.csc_bonding)
        val broadcast = Intent(BROADCAST_BOND_STATE)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDING)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }

    override fun onBonded(device: BluetoothDevice) {
        showToast(R.string.csc_bonded)
        val broadcast = Intent(BROADCAST_BOND_STATE)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDED)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }

    override fun onBondingFailed(device: BluetoothDevice) {
        showToast(R.string.csc_bonding_failed)
        val broadcast = Intent(BROADCAST_BOND_STATE)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }

    override fun onError(device: BluetoothDevice, message: String, errorCode: Int) {
        val broadcast = Intent(BROADCAST_ERROR)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_ERROR_MESSAGE, message)
        broadcast.putExtra(EXTRA_ERROR_CODE, errorCode)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }

    /**
     * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
     *
     * @param messageResId an resource id of the message to be shown
     */
    protected fun showToast(messageResId: Int) {
        handler!!.post {
            Toast.makeText(this@BleProfileService, messageResId, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
     *
     * @param message a message to be shown
     */
    protected fun showToast(message: String?) {
        handler!!.post {
            Toast.makeText(this@BleProfileService, message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Returns the device address
     *
     * @return device address
     */
    protected val deviceAddress: String
        protected get() = bluetoothDevice!!.address

    /**
     * Returns `true` if the device is connected to the sensor.
     *
     * @return `true` if device is connected to the sensor, `false` otherwise
     */
    protected val isConnected: Boolean
        protected get() = bleManager != null && bleManager!!.isConnected

    companion object {
        private const val TAG = "BleProfileService"
        const val BROADCAST_CONNECTION_STATE =
            "no.nordicsemi.android.nrftoolbox.BROADCAST_CONNECTION_STATE"
        const val BROADCAST_SERVICES_DISCOVERED =
            "no.nordicsemi.android.nrftoolbox.BROADCAST_SERVICES_DISCOVERED"
        const val BROADCAST_DEVICE_READY = "no.nordicsemi.android.nrftoolbox.DEVICE_READY"
        const val BROADCAST_BOND_STATE = "no.nordicsemi.android.nrftoolbox.BROADCAST_BOND_STATE"

        @Deprecated("")
        val BROADCAST_BATTERY_LEVEL = "no.nordicsemi.android.nrftoolbox.BROADCAST_BATTERY_LEVEL"
        const val BROADCAST_ERROR = "no.nordicsemi.android.nrftoolbox.BROADCAST_ERROR"

        /**
         * The key for the device name that is returned in [.BROADCAST_CONNECTION_STATE] with state [.STATE_CONNECTED].
         */
        const val EXTRA_DEVICE_NAME = "no.nordicsemi.android.nrftoolbox.EXTRA_DEVICE_NAME"
        const val EXTRA_DEVICE = "no.nordicsemi.android.nrftoolbox.EXTRA_DEVICE"
        const val EXTRA_LOG_URI = "no.nordicsemi.android.nrftoolbox.EXTRA_LOG_URI"
        const val EXTRA_CONNECTION_STATE = "no.nordicsemi.android.nrftoolbox.EXTRA_CONNECTION_STATE"
        const val EXTRA_BOND_STATE = "no.nordicsemi.android.nrftoolbox.EXTRA_BOND_STATE"
        const val EXTRA_SERVICE_PRIMARY = "no.nordicsemi.android.nrftoolbox.EXTRA_SERVICE_PRIMARY"
        const val EXTRA_SERVICE_SECONDARY =
            "no.nordicsemi.android.nrftoolbox.EXTRA_SERVICE_SECONDARY"

        @Deprecated("")
        val EXTRA_BATTERY_LEVEL = "no.nordicsemi.android.nrftoolbox.EXTRA_BATTERY_LEVEL"
        const val EXTRA_ERROR_MESSAGE = "no.nordicsemi.android.nrftoolbox.EXTRA_ERROR_MESSAGE"
        const val EXTRA_ERROR_CODE = "no.nordicsemi.android.nrftoolbox.EXTRA_ERROR_CODE"
        const val STATE_LINK_LOSS = -1
        const val STATE_DISCONNECTED = 0
        const val STATE_CONNECTED = 1
        const val STATE_CONNECTING = 2
        const val STATE_DISCONNECTING = 3
    }
}