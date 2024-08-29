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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.toolbox.libs.profile.ConnectionProvider
import no.nordicsemi.android.toolbox.scanner.repository.ScanningState
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import timber.log.Timber
import javax.inject.Inject

/**
 * This class is responsible for managing the ui states of the scanner screen.
 *
 * @param isScanning True if the scanner is scanning.
 * @param scanningState The current scanning state.
 * @param isDeviceSelected True if a device is selected.
 */
internal data class ScannerUiState(
    val isScanning: Boolean = false,
    val scanningState: ScanningState = ScanningState.Loading,
    val isDeviceSelected: Boolean = false,
)

@HiltViewModel
internal class ScannerViewModel @Inject constructor(
    private val connectionProvider: ConnectionProvider,
    private val navigator: Navigator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState = _uiState.asStateFlow()
    var selectedDevice: Peripheral? = null

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
                // Clear the previous devices list, useful in case of refreshing.
                _uiState.update {
                    it.copy(
                        isScanning = true,
                        scanningState = ScanningState.DevicesDiscovered(emptyList()),
                        isDeviceSelected = false
                    )
                }
            }
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
                _uiState.update {
                    it.copy(
                        isScanning = false
                    )
                }
            }
            .cancellable()
            .catch { e ->
                Timber.e(e)
                // Update the scanning state when an error occurs.
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanningState = ScanningState.Error(e)
                    )
                }
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
            _uiState.update { it.copy(isDeviceSelected = true) }
            viewModelScope.launch {
                connectionProvider.connectAndObservePeripheral(
                    device = peripheral,
                    autoConnect = false,
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
