package no.nordicsemi.android.csc.data

import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.kotlin.ble.profile.csc.data.CSCData

internal data class CSCServiceData(
    val data: CSCData = CSCData(),
    val batteryLevel: Int? = null,
    val connectionState: GattConnectionStateWithStatus? = null,
    val speedUnit: SpeedUnit = SpeedUnit.M_S,
    val deviceName: String? = null,
    val missingServices: Boolean = false
) {

    val disconnectStatus = if (missingServices) {
        BleGattConnectionStatus.NOT_SUPPORTED
    } else {
        connectionState?.status ?: BleGattConnectionStatus.UNKNOWN
    }
}
