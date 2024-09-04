package no.nordicsemi.android.toolbox.libs.profile.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.common.core.simpleSharedFlow
import no.nordicsemi.android.service.DisconnectAndStopEvent
import no.nordicsemi.android.service.ServiceManager
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandler
import no.nordicsemi.android.toolbox.libs.profile.service.ConnectionService
import no.nordicsemi.android.toolbox.libs.profile.service.NoServiceFound
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import javax.inject.Inject
import javax.inject.Singleton

data class PeripheralConnectionState(
    val peripheral: Peripheral? = null,
    val connectionState: ConnectionState? = null,
    val noServiceFound: NoServiceFound? = null
)

@Singleton
class ConnectionRepository @Inject constructor(
    private val serviceManager: ServiceManager,
) {
    private val _peripheralState = MutableStateFlow(PeripheralConnectionState())
    val peripheralState = _peripheralState.asStateFlow()

    private val _profileData = MutableSharedFlow<ByteArray>()
    val profileData = _profileData.asSharedFlow()
    private val _stopEvent = simpleSharedFlow<DisconnectAndStopEvent>()
    internal val stopEvent = _stopEvent.asSharedFlow()

    private val _connectedDevice =
        MutableStateFlow<Map<Peripheral, List<ProfileHandler>>>(emptyMap())
    val connectedDevice = _connectedDevice.asSharedFlow()

    fun connectAndLunchService(
        peripheral: String
    ) {
        serviceManager.startService(ConnectionService::class.java, peripheral)
    }

    fun onConnectionStatusChanged(peripheral: Peripheral, connectionState: ConnectionState) {
        // Update the state of the connection.
        _peripheralState.value = PeripheralConnectionState(peripheral, connectionState)
    }

    // Method to send the profile data to the repository.
    fun onProfileDataReceived(data: ByteArray) {
        _profileData.tryEmit(data)
    }

    fun onProfileStateUpdated(peripheral: Peripheral, noServiceFound: NoServiceFound) {
        // Update the view state to no service found.
        _peripheralState.value =
            PeripheralConnectionState(peripheral, noServiceFound = noServiceFound)
    }

    fun disconnect(peripheral: String) {
        _stopEvent.tryEmit(DisconnectAndStopEvent())
    }

    fun removeDisconnectedDevice(peripheral: Peripheral) {
        _connectedDevice.value = _connectedDevice.value.toMutableMap().apply {
            this.remove(peripheral)
        }
    }

    fun addConnectedDevice(peripheral: Peripheral, handlers: MutableList<ProfileHandler>) {
        _connectedDevice.value = _connectedDevice.value.toMutableMap().apply {
            this[peripheral] = handlers
        }
    }
}