package no.nordicsemi.android.scanner.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import no.nordicsemi.android.scanner.tools.NordicBleScanner
import no.nordicsemi.android.scanner.tools.ScannerStatus
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class NordicBleScannerViewModel @Inject constructor(
    private val bleScanner: NordicBleScanner
) : ViewModel() {

    val state =
        MutableStateFlow(ScannerStatus.PERMISSION_REQUIRED)

    fun onEvent(event: ScannerViewEvent) {
        when (event) {
            ScannerViewEvent.PERMISSION_CHECKED -> onPermissionChecked()
            ScannerViewEvent.BLUETOOTH_ENABLED -> onBluetoothEnabled()
        }.exhaustive
    }

    private fun onPermissionChecked() {
        state.value = bleScanner.getBluetoothStatus()
    }

    private fun onBluetoothEnabled() {
        state.value = bleScanner.getBluetoothStatus()
    }
}

internal enum class ScannerViewEvent {
    PERMISSION_CHECKED, BLUETOOTH_ENABLED
}
