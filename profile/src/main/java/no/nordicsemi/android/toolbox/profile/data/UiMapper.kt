package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.kotlin.ble.core.ConnectionState.Disconnected.Reason

fun toReason(reason:  Reason): String =
    when (reason) {
        Reason.Cancelled -> "Connection was cancelled."
        Reason.LinkLoss -> "Device signal has been lost."
        Reason.Success -> "Device disconnected successfully."
        Reason.TerminateLocalHost -> "Device disconnected by the local host."
        Reason.TerminatePeerUser -> "Device disconnected by the peer user."
        is Reason.Timeout -> "Connection attempt timed out with ${reason.duration}."
        is Reason.Unknown -> "Oops...! Connection went on a coffee break."
        Reason.UnsupportedAddress -> "Device disconnected due to unsupported address."
        Reason.InsufficientAuthentication -> "Device disconnected due to insufficient authentication."
    }