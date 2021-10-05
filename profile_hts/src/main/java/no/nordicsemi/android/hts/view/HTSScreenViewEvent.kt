package no.nordicsemi.android.hts.view

import no.nordicsemi.android.hts.data.TemperatureUnit

internal sealed class HTSScreenViewEvent

internal data class OnTemperatureUnitSelected(val value: TemperatureUnit) : HTSScreenViewEvent()

internal object DisconnectEvent : HTSScreenViewEvent()
