package no.nordicsemi.android.prx.view

internal sealed class PRXScreenViewEvent

internal object NavigateUpEvent : PRXScreenViewEvent()

internal object TurnOnAlert : PRXScreenViewEvent()

internal object TurnOffAlert : PRXScreenViewEvent()

internal object DisconnectEvent : PRXScreenViewEvent()
