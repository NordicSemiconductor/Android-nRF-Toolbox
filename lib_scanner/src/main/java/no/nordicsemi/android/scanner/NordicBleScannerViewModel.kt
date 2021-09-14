package no.nordicsemi.android.scanner

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import no.nordicsemi.android.events.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class NordicBleScannerViewModel @Inject constructor(
    private val bleScanner: NordicBleScanner
) : ViewModel() {

    val state =
        MutableStateFlow(NordicBleScannerState(scannerStatus = ScannerStatus.PERMISSION_REQUIRED))

    val scannerResult = bleScanner.scannerResult

    fun onEvent(event: ScannerViewEvent) {
        when (event) {
            ScannerViewEvent.PERMISSION_CHECKED -> onPermissionChecked()
            ScannerViewEvent.BLUETOOTH_ENABLED -> onBluetoothEnabled()
            ScannerViewEvent.ENABLE_SCANNING -> bleScanner.startScanning()
            ScannerViewEvent.DISABLE_SCANNING -> bleScanner.stopScanning()
        }.exhaustive
    }

    private fun onPermissionChecked() {
        state.value = state.value.copy(scannerStatus = bleScanner.getBluetoothStatus())
    }

    private fun onBluetoothEnabled() {
        state.value = state.value.copy(scannerStatus = bleScanner.getBluetoothStatus())
        bleScanner.startScanning()
    }
}

enum class ScannerViewEvent {
    PERMISSION_CHECKED, BLUETOOTH_ENABLED, ENABLE_SCANNING, DISABLE_SCANNING
}

internal data class NordicBleScannerState(
    val scannerStatus: ScannerStatus
)
