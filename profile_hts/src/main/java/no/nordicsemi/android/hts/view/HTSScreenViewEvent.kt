package no.nordicsemi.android.hts.view

internal sealed class HTSScreenViewEvent

internal data class OnTemperatureUnitSelected(val value: TemperatureUnit) : HTSScreenViewEvent()

internal object DisconnectEvent : HTSScreenViewEvent()

internal object NavigateUp : HTSScreenViewEvent()

internal object OpenLoggerEvent : HTSScreenViewEvent()
