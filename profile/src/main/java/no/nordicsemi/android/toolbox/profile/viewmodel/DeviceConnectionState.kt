package no.nordicsemi.android.toolbox.profile.viewmodel

import no.nordicsemi.android.service.profile.DeviceDisconnectionReason
import no.nordicsemi.android.service.profile.ServiceApi

/**
 * Events triggered by the user from the UI.
 */
internal sealed interface ConnectionEvent {
    data object OnRetryClicked : ConnectionEvent
    data object NavigateUp : ConnectionEvent
    data object DisconnectEvent : ConnectionEvent
    data object OpenLoggerEvent : ConnectionEvent
    data object RequestMaxValueLength : ConnectionEvent
}

/**
 * Represents the state of the UI for the profile screen.
 */
internal sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Disconnected(val reason: DeviceDisconnectionReason?) : ProfileUiState

    data class Connected(
        val deviceData: ServiceApi.DeviceData,
        val isMissingServices: Boolean = false,
        val maxValueLength: Int? = null,
    ) : ProfileUiState
}