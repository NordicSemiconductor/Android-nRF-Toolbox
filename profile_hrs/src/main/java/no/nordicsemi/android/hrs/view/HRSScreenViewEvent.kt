package no.nordicsemi.android.hrs.view

internal sealed class HRSScreenViewEvent

internal object DisconnectEvent : HRSScreenViewEvent()

internal object NavigateUpEvent : HRSScreenViewEvent()

internal object OpenLoggerEvent : HRSScreenViewEvent()
