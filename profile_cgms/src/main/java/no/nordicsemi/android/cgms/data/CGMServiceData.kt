package no.nordicsemi.android.cgms.data

import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.kotlin.ble.profile.cgm.data.CGMRecord
import no.nordicsemi.android.kotlin.ble.profile.gls.data.RequestStatus

internal data class CGMServiceData(
    val records: List<CGMRecordWithSequenceNumber> = emptyList(),
    val batteryLevel: Int? = null,
    val connectionState: GattConnectionStateWithStatus? = null,
    val requestStatus: RequestStatus = RequestStatus.IDLE,
    val deviceName: String? = null,
    val missingServices: Boolean = false
) {

    val disconnectStatus = if (missingServices) {
        BleGattConnectionStatus.NOT_SUPPORTED
    } else {
        connectionState?.status ?: BleGattConnectionStatus.UNKNOWN
    }
}

data class CGMRecordWithSequenceNumber(
    val sequenceNumber: Int,
    val record: CGMRecord,
    val timestamp: Long
)
