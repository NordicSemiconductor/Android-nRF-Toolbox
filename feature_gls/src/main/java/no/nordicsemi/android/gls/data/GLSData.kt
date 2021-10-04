package no.nordicsemi.android.gls.data

internal data class GLSData(
    val record: List<GLSRecord> = emptyList(),
    val batteryLevel: Int = 0,
    val requestStatus: RequestStatus = RequestStatus.IDLE,
    val isDeviceBonded: Boolean = false
)

internal enum class RequestStatus {
    IDLE, PENDING, SUCCESS, ABORTED, FAILED, NOT_SUPPORTED
}
