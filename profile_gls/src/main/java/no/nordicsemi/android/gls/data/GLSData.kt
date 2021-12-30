package no.nordicsemi.android.gls.data

internal data class GLSData(
    val records: List<GLSRecord> = emptyList(),
    val batteryLevel: Int = 0,
    val requestStatus: RequestStatus = RequestStatus.IDLE
)
