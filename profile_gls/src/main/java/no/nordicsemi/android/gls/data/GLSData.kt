package no.nordicsemi.android.gls.data

internal data class GLSData(
    val records: List<GLSRecord> = emptyList(),
    val batteryLevel: Int = 0,
    val requestStatus: RequestStatus = RequestStatus.IDLE,
    val selectedMode: WorkingMode = WorkingMode.ALL
)

internal enum class WorkingMode(val displayName: String) {
    ALL("All"),
    LAST("First"),
    FIRST("Last")
}

internal enum class RequestStatus {
    IDLE, PENDING, SUCCESS, ABORTED, FAILED, NOT_SUPPORTED
}
