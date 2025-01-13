package no.nordicsemi.android.toolbox.libs.core.data.bps

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import java.util.Calendar

enum class BloodPressureType(internal val value: Int) {
    UNIT_MMHG(0),
    UNIT_KPA(1)
}

data class BloodPressureMeasurementData(
    val systolic: Float,
    val diastolic: Float,
    val meanArterialPressure: Float,
    val unit: BloodPressureType,
    val pulseRate: Float?,
    val userID: Int?,
    val status: BPMStatus?,
    val calendar: Calendar?
)

data class IntermediateCuffPressureData(
    @FloatRange(from = 0.0) val cuffPressure: Float,
    val unit: BloodPressureType,
    @FloatRange(from = 0.0) val pulseRate: Float? = null,
    @IntRange(from = 0, to = 255) val userID: Int? = null,
    val status: BPMStatus? = null,
    val calendar: Calendar? = null
)
