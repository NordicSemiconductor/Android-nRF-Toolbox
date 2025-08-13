package no.nordicsemi.android.service.profile

import no.nordicsemi.android.ui.view.internal.DisconnectReason
import no.nordicsemi.kotlin.ble.core.ConnectionState

/** Device disconnection reason. */
sealed interface DeviceDisconnectionReason

/** Includes the [ConnectionState.Disconnected.Reason]. */
data class StateReason(val reason: ConnectionState.Disconnected.Reason) : DeviceDisconnectionReason

/** Includes the custom made [DisconnectReason] to include other disconnection reasons which are not included in the [ConnectionState.Disconnected.Reason]. */
data class CustomReason(val reason: DisconnectReason) :
    DeviceDisconnectionReason
