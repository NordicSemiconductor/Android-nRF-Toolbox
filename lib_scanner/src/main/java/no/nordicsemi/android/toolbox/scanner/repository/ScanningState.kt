package no.nordicsemi.android.toolbox.scanner.repository

import no.nordicsemi.kotlin.ble.client.android.Peripheral

internal sealed interface ScanningState {
    data object Loading : ScanningState
    data class DevicesDiscovered(val devices: List<Peripheral>) : ScanningState
    data class Error(val error: Throwable) : ScanningState
}