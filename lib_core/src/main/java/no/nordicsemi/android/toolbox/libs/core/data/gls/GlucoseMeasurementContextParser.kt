package no.nordicsemi.android.toolbox.libs.core.data.gls

import no.nordicsemi.android.toolbox.libs.core.data.gls.data.Carbohydrate
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSMeasurementContext
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.Health
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.Meal
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.Medication
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.MedicationUnit
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.Tester
import no.nordicsemi.kotlin.data.FloatFormat
import no.nordicsemi.kotlin.data.IntFormat
import no.nordicsemi.kotlin.data.getFloat
import no.nordicsemi.kotlin.data.getInt
import java.nio.ByteOrder

object GlucoseMeasurementContextParser {

    fun parse(
        data: ByteArray,
        byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN
    ): GLSMeasurementContext? {
        if (data.size < 3) return null

        var offset = 0

        val flags: Int = data.getInt(offset++, IntFormat.UINT8)
        val carbohydratePresent = flags and 0x01 != 0
        val mealPresent = flags and 0x02 != 0
        val testerHealthPresent = flags and 0x04 != 0
        val exercisePresent = flags and 0x08 != 0
        val medicationPresent = flags and 0x10 != 0
        val medicationUnitLiter = flags and 0x20 != 0
        val HbA1cPresent = flags and 0x40 != 0
        val extendedFlagsPresent = flags and 0x80 != 0

        if (data.size < (3 + (if (carbohydratePresent) 3 else 0) + (if (mealPresent) 1 else 0) + (if (testerHealthPresent) 1 else 0)
                    + (if (exercisePresent) 3 else 0) + (if (medicationPresent) 3 else 0) + (if (HbA1cPresent) 2 else 0)
                    + if (extendedFlagsPresent) 1 else 0)
        ) {
            return null
        }

        val sequenceNumber: Int = data.getInt(offset, IntFormat.UINT16, byteOrder)
        offset += 2

        // Optional fields
        if (extendedFlagsPresent) {
            // ignore extended flags
            offset += 1
        }

        var carbohydrate: Carbohydrate? = null
        var carbohydrateAmount: Float? = null

        if (carbohydratePresent) {
            val carbohydrateId: Int = data.getInt(offset, IntFormat.UINT8)
            carbohydrate = Carbohydrate.create(carbohydrateId)

            carbohydrateAmount =
                data.getFloat(offset + 1, FloatFormat.IEEE_11073_16_BIT, byteOrder) // in grams
            offset += 3
        }

        var meal: Meal? = null
        if (mealPresent) {
            val mealId: Int = data.getInt(offset, IntFormat.UINT8)
            meal = Meal.create(mealId)
            offset += 1
        }

        var tester: Tester? = null
        var health: Health? = null
        if (testerHealthPresent) {
            val testerAndHealth: Int = data.getInt(offset, IntFormat.UINT8)
            tester = Tester.create(testerAndHealth and 0x0F)
            health = Health.create(testerAndHealth shr 4)
            offset += 1
        }

        var exerciseDuration: Int? = null
        var exerciseIntensity: Int? = null
        if (exercisePresent) {
            exerciseDuration = data.getInt(offset, IntFormat.UINT16, byteOrder) // in seconds
            exerciseIntensity = data.getInt(offset + 2, IntFormat.UINT8) // in percentage
            offset += 3
        }

        var medication: Medication? =
            null
        var medicationAmount: Float? = null
        var medicationUnit: MedicationUnit? = null
        if (medicationPresent) {
            val medicationId: Int = data.getInt(offset, IntFormat.UINT8)
            medication = Medication.create(medicationId)
            medicationAmount =
                data.getFloat(offset + 1, FloatFormat.IEEE_11073_16_BIT, byteOrder) // mg or ml
            medicationUnit =
                if (medicationUnitLiter) MedicationUnit.UNIT_ML else MedicationUnit.UNIT_MG
            offset += 3
        }

        var HbA1c: Float? = null
        if (HbA1cPresent) {
            HbA1c = data.getFloat(offset, FloatFormat.IEEE_11073_16_BIT, byteOrder)
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