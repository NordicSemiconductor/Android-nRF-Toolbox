package no.nordicsemi.android.csc.data

internal sealed class CSCServiceCommand

internal data class SetWheelSizeCommand(val size: Int) : CSCServiceCommand()

internal object DisconnectCommand : CSCServiceCommand()
