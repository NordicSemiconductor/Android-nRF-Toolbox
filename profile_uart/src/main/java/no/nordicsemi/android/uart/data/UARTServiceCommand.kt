package no.nordicsemi.android.uart.data

internal sealed class UARTServiceCommand

internal data class SendTextCommand(val command: String) : UARTServiceCommand()

internal object DisconnectCommand : UARTServiceCommand()
