package no.nordicsemi.android.uart.view

import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.android.uart.data.UARTData
import no.nordicsemi.android.uart.data.UARTMacro

internal data class UARTViewState(
    val macros: List<UARTMacro> = emptyList(),
    val uartManagerState: HTSManagerState = NoDeviceState
)

internal sealed class HTSManagerState

internal data class WorkingState(val result: BleManagerResult<UARTData>) : HTSManagerState()

internal object NoDeviceState : HTSManagerState()
