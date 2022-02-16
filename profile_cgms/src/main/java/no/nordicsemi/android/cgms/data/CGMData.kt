package no.nordicsemi.android.cgms.data

internal data class CGMData(
    val records: List<CGMRecord> = emptyList(),
    val batteryLevel: Int? = null,
    val requestStatus: RequestStatus = RequestStatus.IDLE
)
