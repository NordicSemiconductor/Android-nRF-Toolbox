package no.nordicsemi.android.uart.view

import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.android.uart.data.UARTConfiguration
import no.nordicsemi.android.uart.data.UARTData

internal data class UARTViewState(
    val editedPosition: Int? = null,
    val selectedConfigurationIndex: Int? = null,
    val isConfigurationEdited: Boolean = false,
    val configurations: List<UARTConfiguration> = emptyList(),
    val uartManagerState: HTSManagerState = NoDeviceState
) {
    val showEditDialog: Boolean = editedPosition != null

    val selectedConfiguration: UARTConfiguration? = selectedConfigurationIndex?.let { configurations[it] }
}

internal sealed class HTSManagerState

internal data class WorkingState(val result: BleManagerResult<UARTData>) : HTSManagerState()

internal object NoDeviceState : HTSManagerState()
