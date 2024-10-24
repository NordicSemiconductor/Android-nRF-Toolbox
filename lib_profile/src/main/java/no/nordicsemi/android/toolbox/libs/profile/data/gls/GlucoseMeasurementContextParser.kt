package no.nordicsemi.android.toolbox.libs.profile.data.gls

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.FloatFormat
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.toolbox.libs.profile.data.gls.data.Carbohydrate
import no.nordicsemi.android.toolbox.libs.profile.data.gls.data.GLSMeasurementContext
import no.nordicsemi.android.toolbox.libs.profile.data.gls.data.Health
import no.nordicsemi.android.toolbox.libs.profile.data.gls.data.Meal
import no.nordicsemi.android.toolbox.libs.profile.data.gls.data.Medication
import no.nordicsemi.android.toolbox.libs.profile.data.gls.data.MedicationUnit
import no.nordicsemi.android.toolbox.libs.profile.data.gls.data.Tester

object GlucoseMeasurementContextParser {

    fun parse(bytes: DataByteArray): GLSMeasurementContext? {

        if (bytes.size < 3) {
            return null
        }

        var offset = 0

        val flags: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset++) ?: return null
        val carbohydratePresent = flags and 0x01 != 0
        val mealPresent = flags and 0x02 != 0
        val testerHealthPresent = flags and 0x04 != 0
        val exercisePresent = flags and 0x08 != 0
        val medicationPresent = flags and 0x10 != 0
        val medicationUnitLiter = flags and 0x20 != 0
        val HbA1cPresent = flags and 0x40 != 0
        val extendedFlagsPresent = flags and 0x80 != 0

        if (bytes.size < (3 + (if (carbohydratePresent) 3 else 0) + (if (mealPresent) 1 else 0) + (if (testerHealthPresent) 1 else 0)
                    + (if (exercisePresent) 3 else 0) + (if (medicationPresent) 3 else 0) + (if (HbA1cPresent) 2 else 0)
                    + if (extendedFlagsPresent) 1 else 0)
        ) {
            return null
        }

        val sequenceNumber: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset) ?: return null
        offset += 2

        // Optional fields
        if (extendedFlagsPresent) {
            // ignore extended flags
            offset += 1
        }

        var carbohydrate: Carbohydrate? = null
        var carbohydrateAmount: Float? = null
        if (carbohydratePresent) {
            val carbohydrateId: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset) ?: return null
            carbohydrate = Carbohydrate.create(carbohydrateId)
            carbohydrateAmount = bytes.getFloatValue(FloatFormat.FORMAT_SFLOAT, offset + 1) // in grams
            offset += 3
        }

        var meal: Meal? = null
        if (mealPresent) {
            val mealId: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset) ?: return null
            meal = Meal.create(mealId)
            offset += 1
        }

        var tester: Tester? = null
        var health: Health? = null
        if (testerHealthPresent) {
            val testerAndHealth: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset) ?: return null
            tester = Tester.create(testerAndHealth and 0x0F)
            health = Health.create(testerAndHealth shr 4)
            offset += 1
        }

        var exerciseDuration: Int? = null
        var exerciseIntensity: Int? = null
        if (exercisePresent) {
            exerciseDuration =
                bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset) // in seconds
            exerciseIntensity =
                bytes.getIntValue(IntFormat.FORMAT_UINT8, offset + 2) // in percentage
            offset += 3
        }

        var medication: Medication? =
            null
        var medicationAmount: Float? = null
        var medicationUnit: MedicationUnit? = null
        if (medicationPresent) {
            val medicationId: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset) ?: return null
            medication = Medication.create(medicationId)
            medicationAmount = bytes.getFloatValue(FloatFormat.FORMAT_SFLOAT, offset + 1) // mg or ml
            medicationUnit =
                if (medicationUnitLiter) MedicationUnit.UNIT_ML else MedicationUnit.UNIT_MG
            offset += 3
        }

        var HbA1c: Float? = null
        if (HbA1cPresent) {
            HbA1c = bytes.getFloatValue(FloatFormat.FORMAT_SFLOAT, offset)
            // offset += 2;
        }

        return GLSMeasurementContext(
            sequenceNumber,
            carbohydrate,
            carbohydrateAmount,
            meal,
            tester,
            health,
            exerciseDuration,
            exerciseIntensity,
            medication,
            medicationAmount,
            medicationUnit,
            HbA1c
        )
    }
}