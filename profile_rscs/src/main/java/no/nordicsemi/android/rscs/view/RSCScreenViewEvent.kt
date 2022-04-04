package no.nordicsemi.android.rscs.view

internal sealed class RSCScreenViewEvent

internal object NavigateUpEvent : RSCScreenViewEvent()

internal object DisconnectEvent : RSCScreenViewEvent()

internal object OpenLoggerEvent : RSCScreenViewEvent()
