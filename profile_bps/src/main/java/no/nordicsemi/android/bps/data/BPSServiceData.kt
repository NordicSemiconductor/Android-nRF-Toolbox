package no.nordicsemi.android.bps.data

import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.kotlin.ble.profile.bps.data.BloodPressureMeasurementData
import no.nordicsemi.android.kotlin.ble.profile.bps.data.IntermediateCuffPressureData

data class BPSServiceData (
    val bloodPressureMeasurement: BloodPressureMeasurementData? = null,
    val intermediateCuffPressure: IntermediateCuffPressureData? = null,
    val batteryLevel: Int? = null,
    val connectionState: GattConnectionStateWithStatus? = null
)