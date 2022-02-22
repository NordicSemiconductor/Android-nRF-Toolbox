package no.nordicsemi.android.uart.view

import no.nordicsemi.android.uart.data.UARTConfiguration
import no.nordicsemi.android.uart.data.UARTMacro

internal sealed class UARTViewEvent

internal data class OnEditMacro(val position: Int) : UARTViewEvent()
internal data class OnCreateMacro(val macro: UARTMacro) : UARTViewEvent()
internal data class OnDeleteMacro(val macro: UARTMacro) : UARTViewEvent()
internal object OnEditFinish : UARTViewEvent()

internal data class OnConfigurationSelected(val configuration: UARTConfiguration) : UARTViewEvent()
internal data class OnAddConfiguration(val name: String) : UARTViewEvent()
internal object OnEditConfiguration : UARTViewEvent()
internal object OnDeleteConfiguration : UARTViewEvent()
internal data class OnRunMacro(val macro: UARTMacro) : UARTViewEvent()

internal object DisconnectEvent : UARTViewEvent()

internal object NavigateUp : UARTViewEvent()
