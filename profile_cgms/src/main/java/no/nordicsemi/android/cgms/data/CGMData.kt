package no.nordicsemi.android.cgms.data

internal data class CGMData(
    val records: List<CGMRecord> = emptyList(),
    val batteryLevel: Int = 0,
    val requestStatus: RequestStatus = RequestStatus.IDLE
) {

    fun copyWithNewRecord(record: CGMRecord): CGMData {
        return copy(records = records + record)
    }
}
