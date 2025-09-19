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
            UNKNOWN -> ""
            LOCAL_REQUEST -> "local request" // Indicates that the session was closed because AutoCloseable.close() or RangingSession.stop() was called.
            REMOTE_REQUEST -> "request of a remote peer" // Indicates that the session was closed at the request of a remote peer.
            UNSUPPORTED -> "provided session parameters were not supported"
            SYSTEM_POLICY -> "local system policy forced the session to close" // Indicates that the local system policy forced the session to close, such as power management policy, airplane mode etc.
            NO_PEERS_FOUND -> "none of the specified peers were found" // Indicates that the session was closed because none of the specified peers were found.
        }
    }

    companion object {
        fun getReason(value: Int): String {
            return entries.firstOrNull { it.reason == value }.toString()
        }
    }
}