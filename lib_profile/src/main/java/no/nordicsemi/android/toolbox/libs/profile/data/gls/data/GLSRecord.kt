package no.nordicsemi.android.toolbox.libs.profile.data.gls.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Calendar

@Parcelize
data class GLSRecord(
    val sequenceNumber: Int,
    val time: Calendar? = null,
    val glucoseConcentration: Float? = null,
    val unit: ConcentrationUnit? = null,
    val type: RecordType? = null,
    val status: GlucoseStatus? = null,
    val sampleLocation: SampleLocation? = null,
    val contextInformationFollows: Boolean
) : Parcelable

enum class RecordType(val id: Int) {
    CAPILLARY_WHOLE_BLOOD(1),
    CAPILLARY_PLASMA(2),
    VENOUS_WHOLE_BLOOD(3),
    VENOUS_PLASMA(4),
    ARTERIAL_WHOLE_BLOOD(5),
    ARTERIAL_PLASMA(6),
    UNDETERMINED_WHOLE_BLOOD(7),
    UNDETERMINED_PLASMA(8),
    INTERSTITIAL_FLUID(9),
    CONTROL_SOLUTION(10);

    companion object {
        fun create(value: Int): RecordType {
            return entries.firstOrNull { it.id == value.toInt() }
                ?: throw IllegalArgumentException("Cannot find element for provided value.")
        }

        fun createOrNull(value: Int?): RecordType? {
            return entries.firstOrNull { it.id == value }
        }
    }
}

@Parcelize
data class GLSMeasurementContext(
    val sequenceNumber: Int = 0,
    val carbohydrate: Carbohydrate? = null,
    val carbohydrateAmount: Float? = null,
    val meal: Meal? = null,
    val tester: Tester? = null,
    val health: Health? = null,
    val exerciseDuration: Int? = null,
    val exerciseIntensity: Int? = null,
    val medication: Medication?,
    val medicationQuantity: Float? = null,
    val medicationUnit: MedicationUnit? = null,
    val HbA1c: Float? = null
) : Parcelable

enum class ConcentrationUnit(val id: Int) {
    UNIT_KGPL(0),
    UNIT_MOLPL(1);

    companion object {
        fun create(value: Int): ConcentrationUnit {
            return entries.firstOrNull { it.id == value }
                ?: throw IllegalArgumentException("Cannot find element for provided value.")
        }
    }
}

enum class MedicationUnit(val id: Int) {
    UNIT_MG(0),
    UNIT_ML(1);

    companion object {
        fun create(value: Int): MedicationUnit {
            return entries.firstOrNull { it.id == value }
                ?: throw IllegalArgumentException("Cannot find element for provided value.")
        }
    }
}

enum class SampleLocation(val id: Int) {
    FINGER(1),
    AST(2),
    EARLOBE(3),
    CONTROL_SOLUTION(4),
    NOT_AVAILABLE(15);

    companion object {
        fun createOrNull(value: Int?): SampleLocation? {
            return entries.firstOrNull { it.id == value }
        }
    }
}