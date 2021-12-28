package no.nordicsemi.android.gls.data

internal data class GLSData(
    val records: List<GLSRecord> = emptyList(),
    val batteryLevel: Int = 0,
    val requestStatus: RequestStatus = RequestStatus.IDLE
)

internal enum class WorkingMode {
    ALL,
    LAST,
    FIRST
}

internal enum class RequestStatus {
    IDLE, PENDING, SUCCESS, ABORTED, FAILED, NOT_SUPPORTED
}
