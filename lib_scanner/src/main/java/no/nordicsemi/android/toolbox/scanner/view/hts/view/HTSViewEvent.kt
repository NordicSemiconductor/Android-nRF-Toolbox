package no.nordicsemi.android.toolbox.scanner.view.hts.view

import no.nordicsemi.android.toolbox.libs.profile.data.hts.data.TemperatureUnit

sealed interface HTSScreenViewEvent

internal data class OnTemperatureUnitSelected(
    val value: TemperatureUnit,
) : HTSScreenViewEvent

internal data object OnRetryClicked : HTSScreenViewEvent

internal data object NavigateUp : HTSScreenViewEvent

internal data object DisconnectEvent : HTSScreenViewEvent

internal data object OpenLoggerEvent : HTSScreenViewEvent
