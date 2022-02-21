package no.nordicsemi.android.uart.view

import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.android.uart.data.UARTConfiguration
import no.nordicsemi.android.uart.data.UARTData

internal data class UARTViewState(
    val configuration: List<UARTConfiguration> = emptyList(),
    val uartManagerState: HTSManagerState = NoDeviceState
)

internal sealed class HTSManagerState

internal data class WorkingState(val result: BleManagerResult<UARTData>) : HTSManagerState()

internal object NoDeviceState : HTSManagerState()
