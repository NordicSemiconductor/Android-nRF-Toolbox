package no.nordicsemi.android.toolbox.scanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.toolbox.libs.profile.ConnectionProvider
import no.nordicsemi.android.toolbox.scanner.repository.ScanningState
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class ScannerViewModel @Inject constructor(
    private val connectionProvider: ConnectionProvider,
    private val navigator: Navigator,
) : ViewModel() {
    var selectedDevice: Peripheral? = null
    private val _scanningState = MutableStateFlow<ScanningState>(ScanningState.Loading)
    val scanningState = _scanningState.asStateFlow()

    private var job: Job? = null

    init {
        startScanning()
    }

    /**
     * Starts scanning for BLE devices.
     */
    private fun startScanning() {
        job?.cancel()
        job = connectionProvider.startScanning()
            .onStart {
                // Clear the previous devices list.
                _scanningState.value = ScanningState.DevicesDiscovered(emptyList())
                // Reset the state to loading.
                _scanningState.value = ScanningState.Loading
            }
            .onEach { peripheral ->
                val devices =
                    (_scanningState.value as? ScanningState.DevicesDiscovered)?.devices.orEmpty()
                val isExistingDevice = devices.firstOrNull { it.address == peripheral.address }
                if (isExistingDevice == null) {
                    _scanningState.value = ScanningState.DevicesDiscovered(devices + peripheral)
                }
            }
            .cancellable()
            .catch { e ->
                Timber.e(e)
                _scanningState.value = ScanningState.Error(e)
            }
            .launchIn(viewModelScope)
    }

    /**
     * Callback when a device is selected.
     *
     * @param peripheral The selected peripheral.
     */
    fun onDeviceSelected(peripheral: Peripheral) {
        job?.cancel()
        try {
            selectedDevice = peripheral
            viewModelScope.launch {
                connectionProvider.connectAndObservePeripheral(
                    peripheral,
                    scope = viewModelScope
                )
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    /**
     * Refresh the scanning process.
     */
    fun refreshScanning() {
        startScanning()
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
        // Clear the profile manager to prevent reconnection.
        connectionProvider.clearState()
    }

    /**
     * Navigates back to the previous screen.
     */
    fun navigateBack() {
        navigator.navigateUp()
    }
}
