package no.nordicsemi.android.bps.data

import no.nordicsemi.android.ble.common.profile.bp.BloodPressureTypes
import java.util.*

data class BPSData(
    val batteryLevel: Int = 0,
    val cuffPressure: Float = 0f,
    val unit: Int = 0,
    val pulseRate: Float? = null,
    val userID: Int? = null,
    val status: BloodPressureTypes.BPMStatus? = null,
    val calendar: Calendar? = null,
    val systolic: Float = 0f,
    val diastolic: Float = 0f,
    val meanArterialPressure: Float = 0f,
) {

    fun displaySystolic(): String {
        return "$systolic"
    }

    fun displayDiastolic(): String {
        return "$diastolic"
    }

    fun displayMeanArterialPressure(): String {
        return "$meanArterialPressure"
    }

    fun displayPulse(): String {
        return "$pulseRate"
    }

    fun displayTimeData(): String {
        return ""
    }
}
