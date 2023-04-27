package no.nordicsemi.android.cgms.data

import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.kotlin.ble.profile.cgm.data.CGMRecord
import no.nordicsemi.android.kotlin.ble.profile.gls.data.RequestStatus

internal data class CGMServiceData(
    val records: List<CGMRecordWithSequenceNumber> = emptyList(),
    val batteryLevel: Int? = null,
    val connectionState: GattConnectionStateWithStatus? = null,
    val requestStatus: RequestStatus = RequestStatus.IDLE
)

data class CGMRecordWithSequenceNumber(
    val sequenceNumber: Int,
    val record: CGMRecord,
    val timestamp: Long
)
