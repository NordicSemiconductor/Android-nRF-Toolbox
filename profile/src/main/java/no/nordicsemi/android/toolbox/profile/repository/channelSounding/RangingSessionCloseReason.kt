package no.nordicsemi.android.toolbox.profile.repository.channelSounding

enum class RangingSessionCloseReason(val reason: Int) {
    REASON_UNKNOWN(0),
    REASON_LOCAL_REQUEST(1),
    REASON_REMOTE_REQUEST(2),
    REASON_UNSUPPORTED(3),
    REASON_SYSTEM_POLICY(4),
    REASON_NO_PEERS_FOUND(5), ;

    override fun toString(): String {
        return when (reason) {
            REASON_UNKNOWN.reason -> "Unknown"
            REASON_LOCAL_REQUEST.reason -> "Local request"
            REASON_NO_PEERS_FOUND.reason -> "No peers found"
            REASON_REMOTE_REQUEST.reason -> "Remote request"
            REASON_SYSTEM_POLICY.reason -> "System policy"
            REASON_UNSUPPORTED.reason -> "Unsupported"
            else -> "Unknown reason"
        }
    }

    companion object {
        fun getReason(reason: Int): String {
            return entries.firstOrNull { it.reason == reason }.toString()
        }
    }
}