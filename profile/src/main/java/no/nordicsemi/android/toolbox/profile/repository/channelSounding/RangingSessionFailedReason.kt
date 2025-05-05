package no.nordicsemi.android.toolbox.profile.repository.channelSounding

enum class RangingSessionFailedReason(val reason: Int) {
    UNKNOWN(0),
    LOCAL_REQUEST(1),
    REMOTE_REQUEST(2),
    UNSUPPORTED(3),
    SYSTEM_POLICY(4),
    NO_PEERS_FOUND(5), ;

    override fun toString(): String {
        return when (this) {
            UNKNOWN -> "Unknown"
            LOCAL_REQUEST -> "Local request" // Indicates that the session was closed because AutoCloseable.close() or RangingSession.stop() was called.
            REMOTE_REQUEST -> "Remote request" // Indicates that the session was closed at the request of a remote peer.
            UNSUPPORTED -> "Unsupported" // Indicates that the session closed because the provided session parameters were not supported.
            SYSTEM_POLICY -> "System policy" // Indicates that the local system policy forced the session to close, such as power management policy, airplane mode etc.
            NO_PEERS_FOUND -> "No peers found" // Indicates that the session was closed because none of the specified peers were found.
        }
    }

    companion object {
        fun getReason(value: Int): String {
            return entries.firstOrNull { it.reason == value }.toString()
        }
    }
}