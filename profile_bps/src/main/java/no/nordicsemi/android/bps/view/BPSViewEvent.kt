package no.nordicsemi.android.bps.view

internal sealed class BPSViewEvent

internal object DisconnectEvent : BPSViewEvent()

internal object OpenLoggerEvent : BPSViewEvent()
