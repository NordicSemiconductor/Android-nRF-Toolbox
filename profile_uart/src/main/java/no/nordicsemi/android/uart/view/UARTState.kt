package no.nordicsemi.android.uart.view

import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.android.uart.data.UARTConfiguration
import no.nordicsemi.android.uart.data.UARTData
import no.nordicsemi.android.uart.data.UARTMacro

internal data class UARTViewState(
    val editedPosition: Int? = null,
    val selectedConfigurationName: String? = null,
    val isConfigurationEdited: Boolean = false,
    val configurations: List<UARTConfiguration> = emptyList(),
    val uartManagerState: HTSManagerState = NoDeviceState,
    val isInputVisible: Boolean = true
) {
    val showEditDialog: Boolean = editedPosition != null

    val selectedConfiguration: UARTConfiguration? = configurations.find { selectedConfigurationName == it.name }

    val selectedMacro: UARTMacro? = selectedConfiguration?.let { configuration ->
        editedPosition?.let {
            configuration.macros.getOrNull(it)
        }
    }
}

internal sealed class HTSManagerState

internal data class WorkingState(
    val result: BleManagerResult<UARTData>
) : HTSManagerState()

internal object NoDeviceState : HTSManagerState()
