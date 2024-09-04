package no.nordicsemi.android.hts.viewmodel

import no.nordicsemi.android.toolbox.libs.profile.profile.hts.data.TemperatureUnit

internal sealed interface HTSScreenViewEvent

internal data class OnTemperatureUnitSelected(
    val value: TemperatureUnit,
) : HTSScreenViewEvent

internal data object OnRetryClicked : HTSScreenViewEvent

internal data object NavigateUp : HTSScreenViewEvent

internal data object DisconnectEvent : HTSScreenViewEvent

internal data object OpenLoggerEvent : HTSScreenViewEvent
