package no.nordicsemi.android.bps.data

import no.nordicsemi.android.ble.common.callback.bps.BloodPressureMeasurementResponse
import no.nordicsemi.android.ble.common.callback.bps.IntermediateCuffPressureResponse

internal fun BPSData.copyWithNewResponse(response: IntermediateCuffPressureResponse): BPSData {
    return with (response) {
        copy(
            cuffPressure = cuffPressure,
            unit = unit,
            pulseRate = pulseRate,
            userID = userID,
            status = status,
            calendar = timestamp
        )
    }
}

internal fun BPSData.copyWithNewResponse(response: BloodPressureMeasurementResponse): BPSData {
    return with (response) {
        copy(
            systolic = systolic,
            diastolic = diastolic,
            meanArterialPressure = meanArterialPressure,
            unit = unit,
            pulseRate = pulseRate,
            userID = userID,
            status = status,
        )
    }
}
