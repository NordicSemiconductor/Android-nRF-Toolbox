package no.nordicsemi.android.toolbox.scanner.view.hts.view

import no.nordicsemi.android.toolbox.libs.profile.data.hts.data.TemperatureUnit

sealed interface ProfileScreenViewEvent

internal data class OnTemperatureUnitSelected(
    val value: TemperatureUnit,
) : ProfileScreenViewEvent

internal data class OnRetryClicked(val device: String) : ProfileScreenViewEvent

internal data object NavigateUp : ProfileScreenViewEvent

internal data class DisconnectEvent(val device: String) : ProfileScreenViewEvent

internal data object OpenLoggerEvent : ProfileScreenViewEvent
