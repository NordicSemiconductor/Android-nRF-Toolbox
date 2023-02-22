package no.nordicsemi.android.csc.data

import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.profile.csc.CSCData

data class CSCServicesData(
    val data: CSCData = CSCData(),
    val batteryLevel: Int? = null,
    val connectionState: GattConnectionState = GattConnectionState.STATE_DISCONNECTED
)
