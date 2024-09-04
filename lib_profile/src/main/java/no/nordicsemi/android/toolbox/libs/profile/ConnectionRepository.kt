package no.nordicsemi.android.toolbox.libs.profile

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.common.core.simpleSharedFlow
import no.nordicsemi.android.service.DisconnectAndStopEvent
import no.nordicsemi.android.service.ServiceManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import javax.inject.Inject
import javax.inject.Singleton

data class PeripheralConnectionState(
    val peripheral: Peripheral? = null,
    val connectionState: ConnectionState? = null,
)

@Singleton
class ConnectionRepository @Inject constructor(
    private val serviceManager: ServiceManager,
) {
    private val _peripheralState = MutableStateFlow(PeripheralConnectionState())
    val peripheralState = _peripheralState.asSharedFlow()

    private val _profileData = MutableSharedFlow<Any>()
    val profileData: Flow<Any> = _profileData.asSharedFlow()

    var peripheral: Peripheral? = null
    private val _stopEvent = simpleSharedFlow<DisconnectAndStopEvent>()
    internal val stopEvent = _stopEvent.asSharedFlow()

    private val connectedDevice: MutableMap<Peripheral, MutableList<ProfileHandler>> =
        mutableMapOf()

    fun connectToDeviceAndLunchService(
        peripheral: Peripheral
    ) {
        this.peripheral = peripheral
        serviceManager.startService(ConnectionService::class.java)
    }

    fun onConnectionStatusChanged(connectionState: ConnectionState) {
        // Update the state of the connection.
        _peripheralState.value = PeripheralConnectionState(peripheral, connectionState)
    }

    fun onProfileDataReceived(data: Any) {
        _profileData.tryEmit(data) // Emit the data to be collected elsewhere
    }

    // Method to add or update connected devices and their handlers.
    fun addConnectedDevice(peripheral: Peripheral, handlers: List<ProfileHandler>) {
        connectedDevice[peripheral] = handlers.toMutableList()
    }

    // Method to remove a disconnected device.
    private fun removeDisconnectedDevice(peripheral: Peripheral) {
        connectedDevice.remove(peripheral)
    }

    // Get handlers for a connected peripheral.
    fun getHandlersForPeripheral(peripheral: Peripheral): List<ProfileHandler>? {
        return connectedDevice[peripheral]
    }

    // Cleanup and remove the device if disconnected.
    fun handleDisconnection(peripheral: Peripheral) {
        removeDisconnectedDevice(peripheral)
        if (connectedDevice.isEmpty()) {
            _stopEvent.tryEmit(DisconnectAndStopEvent())
        }
    }
}