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
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.toolbox.lib.utils.logAndReport
import no.nordicsemi.android.toolbox.libs.profile.DeviceConnectionDestinationId
import no.nordicsemi.android.toolbox.scanner.ScannerDestinationId
import no.nordicsemi.android.toolbox.scanner.repository.Scanner
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import timber.log.Timber
import javax.inject.Inject

/**
 * This class is responsible for managing the ui states of the scanner screen.
 *
 * @param isScanning True if the scanner is scanning.
 * @param scanningState The current scanning state.
 */
internal data class ScannerUiState(
    val isScanning: Boolean = false,
    val scanningState: ScanningState = ScanningState.Loading,
)

@HiltViewModel
internal class ScannerViewModel @Inject constructor(
    private val scanner: Scanner,
    private val navigator: Navigator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState = _uiState.asStateFlow()
    private var job: Job? = null

    /**
     * Starts scanning for BLE devices.
     */
    fun startScanning() {
        job?.cancel()
        job = scanner.scan()
            .onEach { peripheral ->
                val devices =
                    _uiState.value.scanningState.let { state ->
                        if (state is ScanningState.DevicesDiscovered) state.devices else emptyList()
                    }
                // Check if the device is already in the list.
                val isExistingDevice = devices.firstOrNull { it.address == peripheral.address }
                // Add the device to the list if it is not already in the list, otherwise ignore it.
                if (isExistingDevice == null) {
                    _uiState.update {
                        it.copy(
                            scanningState = ScanningState.DevicesDiscovered(devices + peripheral)
                        )
                    }
                }
            }
            // Update the scanning state when the scan is completed.
            .onCompletion {
                _uiState.update { it.copy(isScanning = false) }
                job?.cancel()
            }
            .cancellable()
            .catch { e ->
                e.logAndReport()
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanningState = ScanningState.Error(e)
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: UiClickEvent) {
        when (event) {
            OnBackClick -> navigator.navigateUp()
            is OnDeviceSelection -> onDeviceSelected(event.peripheral)
            OnRefreshScan -> refreshScanning()
        }
    }

    /**
     * Callback when a device is selected.
     *
     * @param peripheral The selected peripheral.
     */
    private fun onDeviceSelected(peripheral: Peripheral) {
        try {
            viewModelScope.launch {
                scanner.close()
                navigator.navigateTo(DeviceConnectionDestinationId, peripheral.address)
                {
                    popUpTo(ScannerDestinationId.toString()) {
                        inclusive = true
                    }
                }
            }

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    /**
     * Refresh the scanning process.
     */
    private fun refreshScanning() {
        _uiState.update {
            it.copy(
                isScanning = true,
                scanningState = ScanningState.DevicesDiscovered(emptyList()),
            )
        }
        startScanning()
    }

    private fun clearStates() {
        job?.cancel()
        scanner.close()
    }

    override fun onCleared() {
        super.onCleared()
        clearStates()
    }

}
