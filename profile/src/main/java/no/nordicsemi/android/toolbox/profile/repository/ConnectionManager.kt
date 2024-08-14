package no.nordicsemi.android.toolbox.profile.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.toolbox.profile.HTS_SERVICE_UUID
import no.nordicsemi.android.ui.view.MockRemoteService
import no.nordicsemi.android.ui.view.Profile
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import javax.inject.Inject
import javax.inject.Singleton

internal data class ConnectionViewState(
    val connectionStepState: ConnectionState? = null,
    val isLoading: Boolean = false,
)

@Singleton
internal class ConnectionManager @Inject constructor(
    private val centralManager: CentralManager,
    private val scope: CoroutineScope,
) {
    private val _profile = MutableStateFlow<Profile?>(null)
    val profile = _profile.asStateFlow()
    private val _connectionState = MutableStateFlow(ConnectionViewState())
    val connectionState = _connectionState.asStateFlow()

    suspend fun connect(
        peripheral: Peripheral,
        autoConnect: Boolean = false
    ) {
        centralManager.connect(
            peripheral = peripheral,
            options = if (autoConnect) {
                CentralManager.ConnectionOptions.AutoConnect
            } else CentralManager.ConnectionOptions.Default
        )
        peripheral.state.onEach { state ->
            when (state) {
                ConnectionState.Connected -> {
                    _connectionState.value =
                        _connectionState.value.copy(connectionStepState = ConnectionState.Connected)
                    peripheral.services().onEach { remoteServices ->
                        remoteServices.forEach { service ->
                            when (service.uuid) {
                                HTS_SERVICE_UUID -> {
                                    _profile.value =
                                        Profile.HTS(MockRemoteService(service, state, peripheral))
                                }
                            }
                        }
                    }.launchIn(scope)
                }

                ConnectionState.Connecting -> {
                    _connectionState.value =
                        _connectionState.value.copy(connectionStepState = ConnectionState.Connecting)
                }

                is ConnectionState.Disconnected -> {
                    _connectionState.value = _connectionState.value.copy(
                        connectionStepState = ConnectionState.Disconnected(state.reason)
                    )
                }

                ConnectionState.Disconnecting -> {
                    _connectionState.value =
                        _connectionState.value.copy(connectionStepState = ConnectionState.Disconnecting)
                }
            }
        }.launchIn(scope)
    }

    fun isLoading() {
        _connectionState.value = _connectionState.value.copy(isLoading = true)
    }

    fun disconnect(peripheral: Peripheral) = scope.launch {
        _connectionState.value = ConnectionViewState()
        peripheral.disconnect()
    }
}