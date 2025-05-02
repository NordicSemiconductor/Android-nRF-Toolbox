package no.nordicsemi.android.permissions_ranging.utils

internal sealed class RangingPermissionState {
    /**
     * Ranging is available and the app has the required permissions.
     */
    data object Available : RangingPermissionState()

    /**
     * Ranging is not available.
     */
    data class NotAvailable(
        val reason: RangingNotAvailableReason,
    ) : RangingPermissionState()
}

internal enum class RangingNotAvailableReason {
    /** Ranging is not available on this device. */
    NOT_AVAILABLE,

    /** The app does not have the required permissions. */
    PERMISSION_DENIED,
}