package no.nordicsemi.android.gls.data

internal data class GLSData(
    val records: List<GLSRecord> = emptyList(),
    val batteryLevel: Int? = null,
    val requestStatus: RequestStatus = RequestStatus.IDLE
)
