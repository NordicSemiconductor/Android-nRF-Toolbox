package no.nordicsemi.android.toolbox.libs.profile

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.toolbox.libs.profile.spec.BPS_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.CGMS_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.CSC_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.GLS_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.HRS_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.HTS_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.PRX_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.ProfileModule
import no.nordicsemi.android.toolbox.libs.profile.spec.RSCS_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.UART_SERVICE_UUID
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.distinctByPeripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.util.distinct
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

// TODO: This class will be removed once all profiles are implemented.
sealed class ProfileState {
    /** The profile view state when the profile is loading. */
    data object Loading : ProfileState()

    /** The profile view state when no matching service is found. */
    data object NoServiceFound : ProfileState()

    /** The profile state when the profile is found. */
    data class ProfileFound(val profile: ProfileModule) : ProfileState()

    /** The profile view state when the profile is not implemented yet. */
    data object NotImplementedYet : ProfileState()
}

@Singleton
class ConnectionProvider @Inject constructor(
    private val centralManager: CentralManager,
) {
    var profile: Profile? = null
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState = _profileState.asStateFlow()
    val bleState = centralManager.state
    private val _connectionState = MutableStateFlow<ConnectionState?>(null)
    val connectionState = _connectionState.asStateFlow()

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
     * @param deviceAddress The peripheral to connect to.
     * @param autoConnect If `true`, the connection will be established using the Auto Connect feature.
     */
    suspend fun connectToDevice(
        deviceAddress: String,
        autoConnect: Boolean = false,
        scope: CoroutineScope
    ) {
        val peripheral = getPeripheral(deviceAddress) ?: return
        try {
            if (!peripheral.isDisconnected) return
            clear()
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
                    _connectionState.value = ConnectionState.Connected
                    peripheral.services().onEach { remoteServices ->
                        when {
                            remoteServices.firstOrNull { it.uuid == HTS_SERVICE_UUID } != null -> {
                                val service = remoteServices.first { it.uuid == HTS_SERVICE_UUID }
                                profile = Profile.HTS(
                                    PeripheralDetails(
                                        serviceData = service,
                                        peripheral = peripheral
                                    )
                                )
                                _profileState.value = ProfileState.ProfileFound(
                                    ProfileModule.HTS
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
                                _profileState.value = ProfileState.NotImplementedYet
                                profile = null
                            }

                            else -> {
                                if (remoteServices.isNotEmpty()) {
                                    _profileState.value = ProfileState.NoServiceFound
                                    profile = null
                                }
                            }

                        }
                    }.launchIn(scope)
                }

                ConnectionState.Connecting -> {
                    _connectionState.value = ConnectionState.Connecting
                }

                is ConnectionState.Disconnected -> {
                    _connectionState.value = ConnectionState.Disconnected(state.reason)
                }

                ConnectionState.Disconnecting -> {
                    _connectionState.value = ConnectionState.Disconnecting
                }
            }
        }.launchIn(scope)
    }

    fun getPeripheral(peripheralByAddress: String): Peripheral? {
        return centralManager.getPeripheralById(peripheralByAddress)
    }

    /**
     * Update the ui state to loading.
     */
    fun isLoading() {
        _profileState.value = ProfileState.Loading
    }

    /**
     * Disconnect from the peripheral device.
     *
     * @param peripheral The peripheral device to disconnect from.
     */
    fun disconnect(peripheral: Peripheral, scope: CoroutineScope) = scope.launch {
        // clear all states.
        clear()
        if (peripheral.isConnected) {
            peripheral.disconnect()
            scope.cancel()
        }
    }

    /**
     * Clear states to initial state.
     */
    fun clear() {
        profile = null
        _profileState.value = ProfileState.Loading
        _connectionState.value = null
    }
}