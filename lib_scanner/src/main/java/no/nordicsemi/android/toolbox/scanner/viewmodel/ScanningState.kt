package no.nordicsemi.android.toolbox.scanner.viewmodel

import no.nordicsemi.kotlin.ble.client.android.Peripheral

/** ScanningState represents the state of the scanning process. */
internal sealed interface ScanningState {

    /** Loading state. */
    data object Loading : ScanningState

    /** Devices discovered state.
     *
     * @param devices The list of discovered devices.
     */
    data class DevicesDiscovered(val devices: List<Peripheral>) : ScanningState

    /** Error state.
     *
     * @param error The error that occurred.
     */
    data class Error(val error: Throwable) : ScanningState
}