package no.nordicsemi.android.csc.data

internal sealed class CSCServiceCommand

internal data class SetWheelSizeCommand(val wheelSize: WheelSize) : CSCServiceCommand()

internal object DisconnectCommand : CSCServiceCommand()
