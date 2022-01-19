package no.nordicsemi.android.uart.view

import no.nordicsemi.android.uart.data.UARTData

internal sealed class UARTViewState

internal object LoadingState : UARTViewState()

internal data class DisplayDataState(val data: UARTData) : UARTViewState()
