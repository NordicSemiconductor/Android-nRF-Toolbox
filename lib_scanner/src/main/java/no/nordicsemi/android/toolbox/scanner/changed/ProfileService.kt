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

    // Battery level flow
    private val _batteryLevel = MutableStateFlow<Int?>(null)

    // Missing services flag
    private val _isMissingServices = MutableStateFlow(false)

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val deviceAddress = intent?.getStringExtra(DEVICE_ADDRESS)
        connectToPeripheral(deviceAddress ?: "")
        return super.onStartCommand(intent, flags, startId)
    }

    inner class LocalBinder : Binder(), ServiceApi {
        override val connectedDevices: Flow<Map<Peripheral, List<ProfileHandler>>>
            get() = _connectedDevices.asSharedFlow()

        override val isMissingServices: Flow<Boolean>
            get() = _isMissingServices.asStateFlow()

        override val batteryLevel: Flow<Int?>
            get() = _batteryLevel.asStateFlow()

        override fun getPeripheralById(address: String?): Peripheral? {
            return address?.let { centralManager.getPeripheralById(it) }
        }

        // Disconnect the peripheral
        override fun disconnect(deviceAddress: String) {
            lifecycleScope.launch {
                val peripheral = centralManager.getPeripheralById(deviceAddress)
                peripheral?.let {
                    if (it.isConnected) it.disconnect()
                    // Remove the device from the connected devices map
                    val currentDevices = _connectedDevices.replayCache.firstOrNull() ?: emptyMap()
                    currentDevices[peripheral]?.let {
                        val updatedDevices = currentDevices.toMutableMap().apply {
                            remove(peripheral)
                        }
                        _connectedDevices.tryEmit(updatedDevices)
                    }
                    // Stop the service if no device is connected
                    if (_connectedDevices.replayCache.firstOrNull()?.isEmpty() == true) {
                        stopSelf()
                    }
                }
            }
        }

        override fun getConnectionState(address: String): Flow<ConnectionState>? {
            return getPeripheralById(address)?.state
        }
    }

    // Public method to connect a peripheral (can be called from the client)
    private fun connectToPeripheral(deviceAddress: String) {
        val peripheral = centralManager.getPeripheralById(deviceAddress)
        peripheral?.let {
            connect(it)
            observePeripheralState(it)
        }
    }

    private fun connect(
        device: Peripheral,
    ) {
        // Similar to your existing connect function
        try {
            lifecycleScope.launch {
                centralManager.connect(device)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e(e)
        }
    }

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

    private fun handleConnectedState(
        peripheral: Peripheral,
    ) {
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
            // Check if no service is found.
            if (handlers.isEmpty() && remoteServices.isNotEmpty()) {
                // No service found.
                updateMissingService(peripheral)
                // Stop the service.
                stopSelf()
            }

            // Add the connected device and its handlers to the repository.
            if (handlers.isNotEmpty()) {
                updateConnectedDevices(peripheral, handlers)
                // check if the battery service is available
                val batteryHandler = BatteryHandler()
                remoteServices.firstOrNull { it.uuid == BATTERY_SERVICE_UUID }?.let {
                    lifecycleScope.launch {
                        batteryHandler.apply {
                            handleServices(it, lifecycleScope)
                            batteryLevelData.onEach {
                                _batteryLevel.emit(it)
                            }.launchIn(lifecycleScope)
                        }
                    }
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun updateConnectedDevices(peripheral: Peripheral, handlers: List<ProfileHandler>) {
        val currentDevices = _connectedDevices.replayCache.firstOrNull() ?: emptyMap()
        val updatedDevices = currentDevices.toMutableMap().apply {
            this[peripheral] = handlers
        }
        _connectedDevices.tryEmit(updatedDevices) // Emit the updated device map
    }

    /**
     * Update the missing services flag and remove the device from the connected devices map.
     * @param peripheral the peripheral to update.
     */
    private fun updateMissingService(peripheral: Peripheral) {
        // Remove the device from the connected devices map before updating the missing services flag.
        val currentDevices = _connectedDevices.replayCache.firstOrNull() ?: emptyMap()
        val updatedDevices = currentDevices.toMutableMap().apply {
            remove(peripheral)
        }
        _connectedDevices.tryEmit(updatedDevices)
        // Update the missing services flag
        _isMissingServices.tryEmit(true)
    }

}
