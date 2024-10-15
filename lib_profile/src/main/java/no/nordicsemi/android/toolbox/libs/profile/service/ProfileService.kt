package no.nordicsemi.android.toolbox.libs.profile.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.log.timber.nRFLoggerTree
import no.nordicsemi.android.service.NotificationService
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandler
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandlerFactory
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.CentralManager.ConnectionOptions
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import timber.log.Timber
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi

@AndroidEntryPoint
internal class ProfileService : NotificationService() {

    @Inject
    lateinit var centralManager: CentralManager

    private var logger: nRFLoggerTree? = null

    // Binder to expose methods to the client
    private val binder = LocalBinder()

    private val _connectedDevices =
        MutableStateFlow<Map<String, Pair<Peripheral, List<ProfileHandler>>>>(emptyMap())
    private val _isMissingServices = MutableStateFlow(false)

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        intent?.getStringExtra(DEVICE_ADDRESS)?.let { deviceAddress ->
            initLogger(deviceAddress)
            connectToPeripheral(deviceAddress)
        }
        return START_REDELIVER_INTENT
    }

    inner class LocalBinder : Binder(), ServiceApi {
        override val connectedDevices: Flow<Map<String, Pair<Peripheral, List<ProfileHandler>>>>
            get() = _connectedDevices.asSharedFlow()

        override val isMissingServices: Flow<Boolean>
            get() = _isMissingServices.asStateFlow()

        /**
         * Get the peripheral by its address.
         * @param address the device address.
         * @return the peripheral instance.
         */
        override fun getPeripheralById(address: String?): Peripheral? {
            return address?.let { centralManager.getPeripheralById(it) }
        }

        /**
         * Disconnect the device with the given address.
         * @param deviceAddress the device address to disconnect.
         */
        override fun disconnect(deviceAddress: String) {
            val peripheral = centralManager.getPeripheralById(deviceAddress)
            lifecycleScope.launch {
                peripheral?.let { peripheral ->
                    if (peripheral.isConnected) peripheral.disconnect()
                    removeDevice(peripheral)
                    // clear the flags
                    clearFlags()
                    stopServiceIfNoDevices()
                }
            }
        }

        /**
         * Get the connection state of the device with the given address.
         * @param address the device address.
         * @return the connection state flow.
         */
        override fun getConnectionState(address: String): Flow<ConnectionState>? {
            return getPeripheralById(address)?.state
        }
    }

    /**
     * Connect to the peripheral with the given address.
     * @param deviceAddress the device address.
     */
    private fun connectToPeripheral(deviceAddress: String) {
        centralManager.getPeripheralById(deviceAddress)?.let {
            lifecycleScope.launch {
                try {
                    centralManager.connect(it, options = ConnectionOptions.Direct())
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Could not connect to the device
                    // Since service is started with the device address, stop the service
                    stopForegroundService() // Remove notification from the foreground service
                    Timber.e(e)
                }
                // Observe the peripheral state
                observePeripheralState(it)
            }
        }
    }

    private var job: Job? = null
    /**
     * Observe the peripheral state and handle the connected state.
     * @param peripheral the peripheral to observe.
     */
    private fun observePeripheralState(
        peripheral: Peripheral,
    ) {
        job = peripheral.state.onEach { state ->
            when (state) {
                is ConnectionState.Connected -> {
                    // Handle the connected state
                    handleConnectedState(peripheral)
                }
                is ConnectionState.Closed -> {
                    job?.cancel()
                    anotherJob?.cancel()
                }

                is ConnectionState.Disconnected -> {
                    // Generally the peripheral is disconnected by clicking the disconnect button, but it can also be disconnected by the device itself.
                    // Remove the device from the connected devices map
                    job?.cancel()
                    anotherJob?.cancel()
                    removeDevice(peripheral)
                    // Clear the flags
                    clearFlags()
                    // Stop the service if no devices are connected
                    stopServiceIfNoDevices()
                }

                else -> {
                    // Do nothing
                }
            }
        }.launchIn(lifecycleScope)
    }

    private var anotherJob: Job? = null
    /**
     * Observe the peripheral services and observe the flow of data on each service.
     * @param peripheral the connected peripheral.
     */
    @OptIn(ExperimentalUuidApi::class)
    private fun handleConnectedState(peripheral: Peripheral) {
        val handlers = mutableListOf<ProfileHandler>()
        anotherJob = peripheral.services().onEach { remoteServices ->
            remoteServices.forEach { remoteService ->
                val handler = ProfileHandlerFactory.createHandler(remoteService.uuid)
                handler?.let {
                    Timber.i("Supported service: ${it.profile}")
                    handlers.add(it)
                    lifecycleScope.launch {
                        it.handleServices(remoteService, lifecycleScope)
                    }
                }
            }

            if (remoteServices.isNotEmpty() && handlers.isEmpty())
                _isMissingServices.tryEmit(true)
            else if (handlers.isNotEmpty() && peripheral.isConnected) {
                _isMissingServices.tryEmit(false)
                _connectedDevices.update {
                    it.toMutableMap().apply {
                        this[peripheral.address] = peripheral to handlers
                    }
                }
            }
        }.launchIn(lifecycleScope)
    }

    /**
     * Stop the service if no devices are connected.
     */
    private fun stopServiceIfNoDevices() {
        if (_connectedDevices.value.isEmpty()) {
            stopForegroundService() //// Remove notification from the foreground service
            stopSelf() // Stop the service
        }
    }

    /**
     * Update the connected devices map.
     * @param peripheral the connected peripheral.
     */
    private fun removeDevice(
        peripheral: Peripheral
    ) {
        // Remove the device from the connected devices map before updating the missing services flag.
        _connectedDevices.update {
            it.toMutableMap().apply {
                this.remove(peripheral.address)
            }
        }
    }

    /**
     * Initialize the logger with the device address and plant the logger.
     * @param device the device address.
     */
    private fun initLogger(device: String) {
        logger?.let { Timber.uproot(it) }
        logger = nRFLoggerTree(this, this.getString(R.string.app_name), device)
            .also { Timber.plant(it) }
    }

    /**
     * Uproot the logger and clear the logger instance.
     */
    private fun uprootLogger() {
        logger?.let { Timber.uproot(it) }
        logger = null
    }

    /**
     * Clear the missing services and battery level flags.
     */
    private fun clearFlags() {
        _isMissingServices.tryEmit(false)
        uprootLogger()
    }

}
