package no.nordicsemi.android.toolbox.scanner.changed

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.service.NotificationService
import no.nordicsemi.android.toolbox.libs.profile.handler.BatteryHandler
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandler
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandlerFactory
import no.nordicsemi.android.toolbox.libs.profile.spec.BATTERY_SERVICE_UUID
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ProfileService : NotificationService() {

    @Inject
    lateinit var centralManager: CentralManager

    // Binder to expose methods to the client
    private val binder = LocalBinder()

    private val _connectedDevices =
        MutableStateFlow<Map<Peripheral, List<ProfileHandler>>>(emptyMap())
    private val _batteryLevel = MutableStateFlow<Int?>(null)
    private val _isMissingServices = MutableStateFlow(false)

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra(DEVICE_ADDRESS)?.let { deviceAddress ->
            connectToPeripheral(deviceAddress)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    inner class LocalBinder : Binder(), ServiceApi {
        override val connectedDevices: Flow<Map<Peripheral, List<ProfileHandler>>>
            get() = _connectedDevices.asSharedFlow()

        override val isMissingServices: Flow<Boolean>
            get() = _isMissingServices.asStateFlow()

        override val batteryLevel: Flow<Int?>
            get() = _batteryLevel.asStateFlow()

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
            try {
                lifecycleScope.launch {
                    centralManager.connect(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Timber.e(e)
            }
            // Observe the peripheral state
            observePeripheralState(it)
        }
    }

    /**
     * Observe the peripheral state and handle the connected state.
     * @param peripheral the peripheral to observe.
     */
    private fun observePeripheralState(
        peripheral: Peripheral,
    ) {
        peripheral.state.onEach { state ->
            if (state is ConnectionState.Connected) {
                // Handle the connected state
                handleConnectedState(peripheral)
            }
        }.launchIn(lifecycleScope)
    }

    /**
     * Observe the peripheral services and observe the flow of data on each service.
     * @param peripheral the connected peripheral.
     */
    private fun handleConnectedState(peripheral: Peripheral) {
        peripheral.services().onEach { remoteServices ->
            val handlers = mutableListOf<ProfileHandler>()
            remoteServices.forEach { remoteService ->
                val handler = ProfileHandlerFactory.createHandler(remoteService.uuid)
                handler?.let {
                    handlers.add(it)
                    lifecycleScope.launch {
                        it.handleServices(remoteService, lifecycleScope)
                    }
                }
            }

            // Check if no supported service is found.
            if (handlers.isEmpty() && remoteServices.isNotEmpty()) {
                _isMissingServices.tryEmit(true)
            } else if (handlers.isNotEmpty()) {
                updateConnectedDevices(peripheral, handlers)
                observeBatteryService(remoteServices)
            }
        }.launchIn(lifecycleScope)
    }

    /**
     * Observe the battery service and handle the battery level data.
     * @param services the list of remote services.
     */
    private fun observeBatteryService(services: List<RemoteService>) {
        services.firstOrNull { it.uuid == BATTERY_SERVICE_UUID }?.let { batteryService ->
            BatteryHandler().apply {
                lifecycleScope.launch {
                    handleServices(batteryService, lifecycleScope)
                    batteryLevelData.onEach { _batteryLevel.emit(it) }.launchIn(lifecycleScope)
                }
            }
        }
    }

    /**
     * Stop the service if no devices are connected.
     */
    private fun stopServiceIfNoDevices() {
        if (_connectedDevices.value.isEmpty()) {
            stopSelf()
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
        val currentDevices = _connectedDevices.replayCache.firstOrNull() ?: emptyMap()
        val updatedDevices = currentDevices.toMutableMap().apply {
            remove(peripheral)
        }
        _connectedDevices.tryEmit(updatedDevices)
    }

    private fun updateConnectedDevices(
        peripheral: Peripheral,
        handlers: List<ProfileHandler>
    ) {
        val currentDevices = _connectedDevices.replayCache.firstOrNull() ?: emptyMap()
        val updatedDevices = currentDevices.toMutableMap().apply {
            this[peripheral] = handlers
        }
        _connectedDevices.tryEmit(updatedDevices)
    }

    /**
     * Clear the missing services and battery level flags.
     */
    private fun clearFlags() {
        _isMissingServices.tryEmit(false)
        _batteryLevel.tryEmit(null)
    }


}
