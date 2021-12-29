package no.nordicsemi.android.prx.data

internal sealed class PRXCommand

internal object EnableAlarm : PRXCommand()

internal object DisableAlarm : PRXCommand()
