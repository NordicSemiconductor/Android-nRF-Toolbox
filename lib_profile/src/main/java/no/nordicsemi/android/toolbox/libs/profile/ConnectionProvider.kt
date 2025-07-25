package no.nordicsemi.android.toolbox.libs.profile

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
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

/** A set of supported service UUIDs. */
private val supportedServiceUuids = setOf(
    BPS_SERVICE_UUID, CSC_SERVICE_UUID, CGMS_SERVICE_UUID,
    GLS_SERVICE_UUID, HRS_SERVICE_UUID, PRX_SERVICE_UUID,
    RSCS_SERVICE_UUID, UART_SERVICE_UUID
)

@Singleton
class ConnectionProvider @Inject constructor(
    private val centralManager: CentralManager,
) {
    private val _profile = MutableStateFlow<ServiceProfile?>(null)
    var profile = _profile.asStateFlow()
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
     * Connects to the peripheral device and observe peripheral states.
     *
     * @param deviceAddress The peripheral to connect to.
     * @param autoConnect If `true`, the connection will be established using the Auto Connect feature.
     */
    suspend fun connectAndObservePeripheral(
        deviceAddress: String,
        autoConnect: Boolean = false,
        scope: CoroutineScope
    ) {
        val peripheral = findPeripheralByAddress(deviceAddress) ?: return
        if (!peripheral.isDisconnected) return
        try {
            centralManager.connect(
                peripheral = peripheral,
                options = if (autoConnect) {
                    CentralManager.ConnectionOptions.AutoConnect
                } else CentralManager.ConnectionOptions.Direct()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e(e)
            return
        }
        observerPeripheralState(peripheral, scope)
    }

    private fun observerPeripheralState(
        peripheral: Peripheral,
        scope: CoroutineScope
    ) {
        peripheral.state.onEach { state ->
            when (state) {
                ConnectionState.Connected -> {
                    handleConnectedState(peripheral, scope)
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

    private fun handleConnectedState(
        peripheral: Peripheral,
        scope: CoroutineScope
    ) {
        _connectionState.value = ConnectionState.Connected
        peripheral.services().onEach { remoteServices ->
            when {
                remoteServices.firstOrNull { it.uuid == HTS_SERVICE_UUID } != null -> {
                    val service = remoteServices.first { it.uuid == HTS_SERVICE_UUID }
                    _profile.value = ServiceProfile.HTS(
                        PeripheralDetails(
                            serviceData = service,
                            peripheral = peripheral
                        )
                    )
                    _profileState.value = ProfileState.ProfileFound(
                        ProfileModule.HTS
                    )
                }

                remoteServices.any { it.uuid in supportedServiceUuids } -> {
                    _profileState.value = ProfileState.NotImplementedYet
                }

                else -> {
                    if (remoteServices.isNotEmpty()) {
                        _profileState.value = ProfileState.NoServiceFound
                    }
                }

            }
        }.launchIn(scope)
    }

    /**
     * Find a peripheral device by its address.
     *
     * @param peripheralByAddress The address of the peripheral device.
     * @return The peripheral device if found, or `null` otherwise.
     */
    fun findPeripheralByAddress(peripheralByAddress: String): Peripheral? =
        centralManager.getPeripheralById(peripheralByAddress)


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
        clearState()
        if (peripheral.isConnected) {
            peripheral.disconnect()
            scope.cancel()
        }
    }

    /**
     * Clear states to initial state.
     */
    fun clearState() {
        _profile.value
        _profileState.value = ProfileState.Loading
        _connectionState.value = null
    }
}