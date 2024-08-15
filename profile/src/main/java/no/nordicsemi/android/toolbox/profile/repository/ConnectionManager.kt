package no.nordicsemi.android.toolbox.profile.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.toolbox.profile.BPS_SERVICE_UUID
import no.nordicsemi.android.toolbox.profile.CGMS_SERVICE_UUID
import no.nordicsemi.android.toolbox.profile.CSC_SERVICE_UUID
import no.nordicsemi.android.toolbox.profile.GLS_SERVICE_UUID
import no.nordicsemi.android.toolbox.profile.HRS_SERVICE_UUID
import no.nordicsemi.android.toolbox.profile.HTS_SERVICE_UUID
import no.nordicsemi.android.toolbox.profile.PRX_SERVICE_UUID
import no.nordicsemi.android.toolbox.profile.RSCS_SERVICE_UUID
import no.nordicsemi.android.toolbox.profile.UART_SERVICE_UUID
import no.nordicsemi.android.ui.view.MockRemoteService
import no.nordicsemi.android.ui.view.Profile
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class is responsible for managing the ui states of connection to the peripheral device.
 *
 * @param connectionState The current connection state.
 * @param profileViewState The current profile view state.
 */
internal data class UiViewState(
    val connectionState: ConnectionState? = null,
    val profileViewState: ProfileViewState = ProfileViewState.Loading,
)

/**
 * This class is responsible for managing the profile view states.
 */
internal sealed class ProfileViewState {
    /** The profile view state when the profile is loading. */
    data object Loading : ProfileViewState()

    /** The profile view state when no matching service is found. */
    data object NoServiceFound : ProfileViewState()

    /**
     * The profile view state when a matching profile is found.
     *
     * @param profile The matching profile.
     */
    data class ProfileFound(val profile: Profile? = null) : ProfileViewState()

    /** The profile view state when the profile is not implemented yet. */
    // TODO: This state will be removed once the profile is implemented.
    data object NotImplemented : ProfileViewState()
}

@Singleton
internal class ConnectionManager @Inject constructor(
    private val centralManager: CentralManager,
    private val scope: CoroutineScope,
) {
    private val _uiViewState = MutableStateFlow(UiViewState())
    val uiViewState = _uiViewState.asStateFlow()

    /**
     * Connect to the peripheral device.
     *
     * @param peripheral The peripheral device to connect to.
     * @param autoConnect True to auto connect to the peripheral device, false otherwise.
     */
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
                    _uiViewState.value =
                        _uiViewState.value.copy(connectionState = ConnectionState.Connected)
                    peripheral.services().onEach { remoteServices ->
                        remoteServices.forEach { service ->
                            when (service.uuid) {
                                HTS_SERVICE_UUID -> {
                                    _uiViewState.value = _uiViewState.value.copy(
                                        profileViewState = ProfileViewState.ProfileFound(
                                            Profile.HTS(MockRemoteService(service, peripheral))
                                        ),
                                    )
                                }

                                BPS_SERVICE_UUID -> {
                                    _uiViewState.value = _uiViewState.value.copy(
                                        profileViewState = ProfileViewState.NotImplemented
                                    )
                                }

                                CSC_SERVICE_UUID -> {
                                    _uiViewState.value = _uiViewState.value.copy(
                                        profileViewState = ProfileViewState.NotImplemented
                                    )
                                }

                                CGMS_SERVICE_UUID -> {
                                    _uiViewState.value = _uiViewState.value.copy(
                                        profileViewState = ProfileViewState.NotImplemented
                                    )
                                }

                                GLS_SERVICE_UUID -> {
                                    _uiViewState.value = _uiViewState.value.copy(
                                        profileViewState = ProfileViewState.NotImplemented
                                    )
                                }

                                HRS_SERVICE_UUID -> {
                                    _uiViewState.value = _uiViewState.value.copy(
                                        profileViewState = ProfileViewState.NotImplemented
                                    )
                                }

                                PRX_SERVICE_UUID -> {
                                    _uiViewState.value = _uiViewState.value.copy(
                                        profileViewState = ProfileViewState.NotImplemented
                                    )
                                }

                                RSCS_SERVICE_UUID -> {
                                    _uiViewState.value = _uiViewState.value.copy(
                                        profileViewState = ProfileViewState.NotImplemented
                                    )
                                }

                                UART_SERVICE_UUID -> {
                                    _uiViewState.value = _uiViewState.value.copy(
                                        profileViewState = ProfileViewState.NotImplemented
                                    )
                                }

                                else -> {
                                    _uiViewState.value = _uiViewState.value.copy(
                                        profileViewState = ProfileViewState.NoServiceFound,
                                    )
                                }
                            }
                        }
                    }.launchIn(scope)
                }

                ConnectionState.Connecting -> {
                    _uiViewState.value = _uiViewState.value.copy(
                        connectionState = ConnectionState.Connecting
                    )
                }

                is ConnectionState.Disconnected -> {
                    _uiViewState.value = _uiViewState.value.copy(
                        connectionState = ConnectionState.Disconnected(state.reason)
                    )
                }

                ConnectionState.Disconnecting -> {
                    _uiViewState.value = _uiViewState.value.copy(
                        connectionState = ConnectionState.Disconnecting
                    )
                }
            }
        }.launchIn(scope)
    }

    /**
     * Update the ui state to loading.
     */
    fun isLoading() {
        _uiViewState.value = _uiViewState.value.copy(
            profileViewState = ProfileViewState.Loading,
        )
    }

    /**
     * Disconnect from the peripheral device.
     *
     * @param peripheral The peripheral device to disconnect from.
     */
    fun disconnect(peripheral: Peripheral) = scope.launch {
        // clear all states.
        _uiViewState.value = UiViewState()
        peripheral.disconnect()
    }
}