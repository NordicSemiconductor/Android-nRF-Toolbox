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
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.toolbox.profile.ProfileArgs
import no.nordicsemi.android.toolbox.profile.ProfileDestinationId
import no.nordicsemi.android.toolbox.scanner.repository.ScanningManager
import no.nordicsemi.android.toolbox.scanner.repository.ScanningState
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class ScannerViewModel @Inject constructor(
    private val bleScanner: ScanningManager,
    private val navigator: Navigator,
) : ViewModel() {
    private val _bleScanningState = MutableStateFlow<ScanningState>(ScanningState.Loading)
    val bleScanningState = _bleScanningState.asStateFlow()

    private var job: Job? = null

    /**
     * Starts scanning for BLE devices.
     */
    fun startScanning() {
        job?.cancel()
        job = bleScanner.startScanning()
            .onStart {
                _bleScanningState.value = ScanningState.DevicesDiscovered(emptyList())
                _bleScanningState.value = ScanningState.Loading
            }
            .onEach { peripheral ->
                val devices =
                    (bleScanningState.value as? ScanningState.DevicesDiscovered)?.devices.orEmpty()
                val isExistingDevice = devices.firstOrNull { it.address == peripheral.address }
                if (isExistingDevice == null) {
                    _bleScanningState.value = ScanningState.DevicesDiscovered(devices + peripheral)
                }
            }
            .cancellable()
            .catch { e ->
                Timber.e(e)
                _bleScanningState.value = ScanningState.Error(e)
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
            navigator.navigateTo(to = ProfileDestinationId, args = ProfileArgs(peripheral))
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
    }
}
