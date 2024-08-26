package no.nordicsemi.android.toolbox.libs.profile

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.ui.view.BPS_SERVICE_UUID
import no.nordicsemi.android.ui.view.CGMS_SERVICE_UUID
import no.nordicsemi.android.ui.view.CSC_SERVICE_UUID
import no.nordicsemi.android.ui.view.GLS_SERVICE_UUID
import no.nordicsemi.android.ui.view.HRS_SERVICE_UUID
import no.nordicsemi.android.ui.view.HTS_SERVICE_UUID
import no.nordicsemi.android.ui.view.PRX_SERVICE_UUID
import no.nordicsemi.android.ui.view.RSCS_SERVICE_UUID
import no.nordicsemi.android.ui.view.UART_SERVICE_UUID
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.distinctByPeripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.util.distinct
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

/**
 * This class is responsible for managing the ui states of connection to the peripheral device.
 *
 * @param connectionState The current connection state.
 * @param profileViewState The current profile view state.
 */
data class UiViewState(
    val connectionState: ConnectionState? = null,
    val profileViewState: ProfileViewState = ProfileViewState.Loading,
)

/**
 * This class is responsible for managing the profile view states.
 */
sealed class ProfileViewState {
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
class ConnectionProvider @Inject constructor(
    private val centralManager: CentralManager,
) {
    private val _uiViewState = MutableStateFlow(UiViewState())
    val uiViewState = _uiViewState.asStateFlow()

    val state = centralManager.state
    private var connectedPeripheral: Peripheral? = null

    /**
     * Scans for BLE devices.
     *
     * @return A flow of [Peripheral] devices.
     */
    fun startScanning(): Flow<Peripheral> {
        return centralManager.scan(2000.milliseconds)
            .filter { it.isConnectable }
            .distinctByPeripheral()
            .map { it.peripheral }
            .distinct()
            .catch { e -> Timber.e(e) }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Connects to the peripheral device.
     *
     * @param peripheral The peripheral to connect to.
     * @param autoConnect If `true`, the connection will be established using the Auto Connect feature.
     */
    suspend fun connectToDevice(
        peripheral: Peripheral,
        autoConnect: Boolean = false,
        scope: CoroutineScope
    ) {
        try {
            if (!peripheral.isDisconnected) return
            centralManager.connect(
                peripheral = peripheral,
                options = if (autoConnect) {
                    CentralManager.ConnectionOptions.AutoConnect
                } else CentralManager.ConnectionOptions.Direct()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e(e)
        }
        peripheral.state.onEach { state ->
            when (state) {
                ConnectionState.Connected -> {
                    _uiViewState.value = _uiViewState.value.copy(
                        connectionState = ConnectionState.Connected,
                        profileViewState = ProfileViewState.Loading
                    )
                    peripheral.services().onEach { remoteServices ->
                        when {
                            remoteServices.firstOrNull { it.uuid == HTS_SERVICE_UUID } != null -> {
                                val service = remoteServices.first { it.uuid == HTS_SERVICE_UUID }
                                _uiViewState.value = _uiViewState.value.copy(
                                    profileViewState = ProfileViewState.ProfileFound(
                                        Profile.HTS(MockRemoteService(service, peripheral))
                                    )
                                )
                            }

                            remoteServices.firstOrNull { it.uuid == BPS_SERVICE_UUID } != null ||
                                    remoteServices.firstOrNull { it.uuid == CSC_SERVICE_UUID } != null ||
                                    remoteServices.firstOrNull { it.uuid == CGMS_SERVICE_UUID } != null ||
                                    remoteServices.firstOrNull { it.uuid == GLS_SERVICE_UUID } != null ||
                                    remoteServices.firstOrNull { it.uuid == HRS_SERVICE_UUID } != null ||
                                    remoteServices.firstOrNull { it.uuid == PRX_SERVICE_UUID } != null ||
                                    remoteServices.firstOrNull { it.uuid == RSCS_SERVICE_UUID } != null ||
                                    remoteServices.firstOrNull { it.uuid == UART_SERVICE_UUID } != null -> {
                                _uiViewState.value = _uiViewState.value.copy(
                                    profileViewState = ProfileViewState.NotImplemented
                                )
                            }

                            else -> {
                                if (remoteServices.isNotEmpty())
                                    _uiViewState.value = _uiViewState.value.copy(
                                        profileViewState = ProfileViewState.NoServiceFound
                                    )
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
    fun disconnect(peripheral: Peripheral, scope: CoroutineScope) = scope.launch {
        // clear all states.
        _uiViewState.value = UiViewState()
        if (peripheral.isConnected) peripheral.disconnect()
    }

    /**
     * Clear states to initial state.
     */
    fun clear() {
        _uiViewState.value = UiViewState()
    }
}