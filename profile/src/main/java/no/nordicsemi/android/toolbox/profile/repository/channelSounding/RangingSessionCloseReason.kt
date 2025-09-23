package no.nordicsemi.android.toolbox.profile.repository.channelSounding

enum class RangingSessionCloseReason(val reason: Int) {
    REASON_UNKNOWN(0),
    REASON_LOCAL_REQUEST(1),
    REASON_REMOTE_REQUEST(2),
    REASON_UNSUPPORTED(3),
    REASON_SYSTEM_POLICY(4),
    REASON_NO_PEERS_FOUND(5), ;

    override fun toString(): String {
        return when (this) {
            REASON_UNKNOWN -> ""
            REASON_LOCAL_REQUEST -> "local request" // Indicates that the session was closed because AutoCloseable.close() or RangingSession.stop() was called.
            REASON_REMOTE_REQUEST -> "request of a remote peer" // Indicates that the session was closed at the request of a remote peer.
            REASON_UNSUPPORTED -> "provided session parameters were not supported"
            REASON_SYSTEM_POLICY -> "local system policy forced the session to close" // Indicates that the local system policy forced the session to close, such as power management policy, airplane mode etc.
            REASON_NO_PEERS_FOUND -> "none of the specified peers were found" // Indicates that the session was closed because none of the specified peers were found.

        }
    }

    companion object {
        fun getReason(reason: Int): String {
            return entries.firstOrNull { it.reason == reason }.toString()
        }
    }
}