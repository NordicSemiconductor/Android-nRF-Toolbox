package no.nordicsemi.android.nrftoolbox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.hts.HTSDestinationId
import no.nordicsemi.android.nrftoolbox.data.BPS_SERVICE_UUID
import no.nordicsemi.android.nrftoolbox.data.HTS_SERVICE_UUID
import no.nordicsemi.android.toolbox.scanner.MockRemoteService
import no.nordicsemi.android.toolbox.scanner.Profile
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.distinctByPeripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.util.distinct
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

data class HomeViewState(
    val isScanning: Boolean = false,
    val devices: List<Peripheral> = emptyList(),
)

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val centralManager: CentralManager,
    private val navigator: Navigator,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeViewState())
    val viewState = _state.asStateFlow()
    private var job: Job? = null

    fun startScanning() {
        _state.value = _state.value.copy(
            isScanning = true,
            devices = emptyList()
        )
        job = centralManager.scan(2000.milliseconds)
            .distinctByPeripheral()
            .map { it.peripheral }
            .distinct()
            .onEach { device -> checkForExistingDevice(device) }
            .catch { e -> Timber.e(e) }
            .onCompletion {
                job?.cancel()
                _state.value = _state.value.copy(isScanning = false)
            }.launchIn(viewModelScope)
    }

    private fun checkForExistingDevice(peripheral: Peripheral) {
        val devices = _state.value.devices.toMutableList()
        val existingDevice = devices.firstOrNull { it.address == peripheral.address }
        if (existingDevice == null) {
            devices.add(peripheral)
            _state.value = _state.value.copy(devices = devices)
        }
    }

    fun connect(peripheral: Peripheral, autoConnect: Boolean = false) = viewModelScope.launch {
        // If scanning is in progress, stop it.
        stopScanning()
        // Initiate the connection with the appropriate options.
        centralManager.connect(
            peripheral = peripheral,
            options = if (autoConnect) {
                CentralManager.ConnectionOptions.AutoConnect
            } else CentralManager.ConnectionOptions.Default
        )

        // Observe the connection state of the peripheral.
        peripheral.state
            .filter { it == ConnectionState.Connected }
            .flatMapConcat { peripheral.services() }
            .onEach { remoteServices ->
                remoteServices.firstOrNull { remoteService ->
                    when (remoteService.uuid) {
                        HTS_SERVICE_UUID -> {
                            handleHTSService(remoteService, peripheral)
                            true
                        }

                        BPS_SERVICE_UUID -> {
                            handleBPSService(remoteService)
                            true
                        }

                        else -> {
                            Timber.tag("Unknown Service").d("Service: $remoteService")
                            false
                        }
                    }
                }
            }
            .catch { e -> Timber.e(e) }
            .launchIn(this)
    }

    private fun handleHTSService(remoteService: RemoteService, peripheral: Peripheral) {
        // HTS service found, navigate to the HTS screen
        navigator.navigateTo(
            HTSDestinationId, Profile.HTS(
                MockRemoteService(
                    remoteService,
                    peripheral.state,
                    peripheral
                )
            )
        )
        job?.cancel()
    }

    private fun handleBPSService(remoteService: RemoteService) {
        // BPS service found
        Timber.tag("Service Found").d("BPS Service found $remoteService")
    }

    private fun closeCentralManager() {
        stopScanning()
        centralManager.close()
    }

    private fun stopScanning() {
        if (_state.value.isScanning) {
            job?.cancel()
            _state.value = _state.value.copy(isScanning = false)
        }
    }

    public override fun onCleared() {
        super.onCleared()
        closeCentralManager()
    }

}