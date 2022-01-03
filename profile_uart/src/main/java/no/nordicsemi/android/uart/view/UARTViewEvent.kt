package no.nordicsemi.android.uart.view

import no.nordicsemi.android.uart.data.UARTMacro

internal sealed class UARTViewEvent

internal data class OnCreateMacro(val macro: UARTMacro) : UARTViewEvent()
internal data class OnDeleteMacro(val macro: UARTMacro) : UARTViewEvent()

internal data class OnRunMacro(val macro: UARTMacro) : UARTViewEvent()

internal object OnDisconnectButtonClick : UARTViewEvent()
