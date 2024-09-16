package no.nordicsemi.android.toolbox.scanner.view.hts.view

import no.nordicsemi.android.toolbox.libs.profile.data.hts.data.TemperatureUnit
import no.nordicsemi.kotlin.ble.client.android.Peripheral

sealed interface ProfileScreenViewEvent

internal data class OnTemperatureUnitSelected(
    val value: TemperatureUnit,
) : ProfileScreenViewEvent

internal data object OnRetryClicked : ProfileScreenViewEvent

internal data object NavigateUp : ProfileScreenViewEvent

internal data class DisconnectEvent(val device: String) : ProfileScreenViewEvent

internal data object OpenLoggerEvent : ProfileScreenViewEvent
