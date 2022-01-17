package no.nordicsemi.android.uart.view

import no.nordicsemi.android.uart.data.UARTData

internal data class UARTState(
    val viewState: UARTViewState,
    val isActive: Boolean = true
)

internal sealed class UARTViewState

internal object LoadingState : UARTViewState()

internal data class DisplayDataState(val data: UARTData) : UARTViewState()
