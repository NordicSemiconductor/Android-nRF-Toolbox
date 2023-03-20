package no.nordicsemi.android.csc.data

import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.profile.csc.data.CSCData

internal data class CSCServiceData(
    val data: CSCData = CSCData(),
    val batteryLevel: Int? = null,
    val connectionState: GattConnectionState? = null,
    val speedUnit: SpeedUnit = SpeedUnit.M_S,
    val deviceName: String? = null
)
