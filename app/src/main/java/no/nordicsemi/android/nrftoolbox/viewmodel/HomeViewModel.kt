package no.nordicsemi.android.nrftoolbox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
        _state.value = _state.value.copy(isScanning = true)
        job = centralManager.scan(5000.milliseconds)
            .distinctByPeripheral()
            .map {
                it.peripheral
            }
            .distinct()
            .onEach { device ->
                checkForExistingDevice(device)
            }
            .catch { e ->
                Timber.e(e)
            }
            .onCompletion {
                _state.value = _state.value.copy(
                    isScanning = false,
                )
            }
            .launchIn(viewModelScope)
    }

    private fun checkForExistingDevice(peripheral: Peripheral) {
        val devices = _state.value.devices.toMutableList()
        val existingDevice = devices.firstOrNull { it.address == peripheral.address }
        if (existingDevice != null) {
            val index = devices.indexOf(existingDevice)
            devices[index] = peripheral
            _state.value = _state.value.copy(devices = devices)
        } else {
            devices.add(peripheral)
            _state.value = _state.value.copy(devices = devices)
        }
    }

    private fun stopScanning() {
        if (_state.value.isScanning) {
            job?.cancel()
            _state.value = _state.value.copy(isScanning = false)
        }
    }

    fun connect(peripheral: Peripheral, autoConnect: Boolean = false) = viewModelScope.launch {
        stopScanning()
        centralManager.connect(
            peripheral = peripheral,
            options = if (autoConnect) {
                CentralManager.ConnectionOptions.AutoConnect
            } else CentralManager.ConnectionOptions.Default
        )
        peripheral.state
            .onEach {
                if (it == ConnectionState.Connected) {
                    peripheral.services().onEach { remoteServices ->
                        remoteServices.forEach { remoteService ->
                            when (remoteService.uuid) {
                                HTS_SERVICE_UUID -> {
                                    // HTS service found
                                    // Navigate to the HTS screen.
                                    navigator.navigateTo(
                                        HTSDestinationId, Profile.HTS(
                                            MockRemoteService(
                                                remoteService,
                                                peripheral.state,
                                                peripheral,
                                            )
                                        )
                                    )
                                    job?.cancel()
                                }

                                BPS_SERVICE_UUID -> {
                                    Timber.tag("BBB").d("BPS Service found.")
                                    // BPS service found
                                    // Navigate to the BPS screen.
                                }
                            }
                        }
                    }.launchIn(this)
                }
            }
            .catch { e -> Timber.e(e) }
            .launchIn(this)

    }

    private fun closeCentralManager() {
        if (job?.isActive == true) job?.cancel()
        centralManager.close()
    }

    public override fun onCleared() {
        super.onCleared()
        closeCentralManager()
    }

}