/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.nordicsemi.android.gls.data

import java.util.*

internal data class GLSRecord(
    /** Record sequence number  */
    val sequenceNumber: Int = 0,

    /** The base time of the measurement  */
    val time: Calendar? = null,

    /** The glucose concentration. 0 if not present  */
    val glucoseConcentration: Float = 0f,

    /** Concentration unit. One of the following: [ConcentrationUnit.UNIT_KGPL], [ConcentrationUnit.UNIT_MOLPL]  */
    val unit: ConcentrationUnit = ConcentrationUnit.UNIT_KGPL,

    val type: RecordType?,

    /** The sample location. 0 if unknown  */
    val sampleLocation: Int = 0,

    /** Sensor status annunciation flags. 0 if not present  */
    val status: Int = 0,

    var context: MeasurementContext? = null
)

internal enum class RecordType(val id: Int) {
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
            return values().firstOrNull { it.id == value.toInt() }
                ?: throw IllegalArgumentException("Cannot find element for provided value.")
        }

        fun createOrNull(value: Int?): RecordType? {
            return values().firstOrNull { it.id == value }
        }
    }
}

internal data class MeasurementContext(
    /** Record sequence number  */
    val sequenceNumber: Int = 0,

    val carbohydrateId: CarbohydrateId = CarbohydrateId.NOT_PRESENT,

    /** Number of kilograms of carbohydrate  */
    val carbohydrateUnits: Float = 0f,

    val meal: TypeOfMeal = TypeOfMeal.NOT_PRESENT,

    val tester: TestType = TestType.NOT_PRESENT,

    val health: HealthStatus = HealthStatus.NOT_PRESENT,

    /** Exercise duration in seconds. 0 if not present  */
    val exerciseDuration: Int = 0,

    /** Exercise intensity in percent. 0 if not present  */
    val exerciseIntensity: Int = 0,

    val medicationId: MedicationId = MedicationId.NOT_PRESENT,

    /** Quantity of medication. See [.medicationUnit] for the unit.  */
    val medicationQuantity: Float = 0f,

    /** One of the following: [MeasurementContext.UNIT_kg], [MeasurementContext.UNIT_l].  */
    val medicationUnit: MedicationUnit = MedicationUnit.UNIT_KG,

    /** HbA1c value. 0 if not present  */
    val HbA1c: Float = 0f
)

internal enum class ConcentrationUnit(val id: Int) {
    UNIT_KGPL(0),
    UNIT_MOLPL(1);

    companion object {
        fun create(value: Int): ConcentrationUnit {
            return values().firstOrNull { it.id == value }
                ?: throw IllegalArgumentException("Cannot find element for provided value.")
        }
    }
}

internal enum class CarbohydrateId(val id: Int) {
    NOT_PRESENT(0),
    BREAKFAST(1),
    LUNCH(2),
    DINNER(3),
    SNACK(4),
    DRINK(5),
    SUPPER(6),
    BRUNCH(7);

    companion object {
        fun create(value: Byte): CarbohydrateId {
            return values().firstOrNull { it.id == value.toInt() }
                ?: throw IllegalArgumentException("Cannot find element for provided value.")
        }
    }
}

internal enum class TypeOfMeal(val id: Int) {
    NOT_PRESENT(0),
    PREPRANDIAL(1),
    POSTPRANDIAL(2),
    FASTING(3),
    CASUAL(4),
    BEDTIME(5);

    companion object {
        fun create(value: Byte): TypeOfMeal {
            return values().firstOrNull { it.id == value.toInt() }
                ?: throw IllegalArgumentException("Cannot find element for provided value.")
        }
    }
}

internal enum class TestType(val id: Int) {
    NOT_PRESENT(0),
    SELF(1),
    HEALTH_CARE_PROFESSIONAL(2),
    LAB_TEST(3),
    VALUE_NOT_AVAILABLE(15);

    companion object {
        fun create(value: Byte): TestType {
            return values().firstOrNull { it.id == value.toInt() }
                ?: throw IllegalArgumentException("Cannot find element for provided value.")
        }
    }
}

internal enum class HealthStatus(val id: Int) {
    NOT_PRESENT(0),
    MINOR_HEALTH_ISSUES(1),
    MAJOR_HEALTH_ISSUES(2),
    DURING_MENSES(3),
    UNDER_STRESS(4),
    NO_HEALTH_ISSUES(5),
    VALUE_NOT_AVAILABLE(15);

    companion object {
        fun create(value: Byte): HealthStatus {
            return values().firstOrNull { it.id == value.toInt() }
                ?: throw IllegalArgumentException("Cannot find element for provided value.")
        }
    }
}

internal enum class MedicationId(val id: Int) {
    NOT_PRESENT(0),
    RAPID_ACTING_INSULIN(1),
    SHORT_ACTING_INSULIN(2),
    INTERMEDIATE_ACTING_INSULIN(3),
    LONG_ACTING_INSULIN(4),
    PRE_MIXED_INSULIN(5);

    companion object {
        fun create(value: Byte): MedicationId {
            return values().firstOrNull { it.id == value.toInt() }
                ?: throw IllegalArgumentException("Cannot find element for provided value.")
        }
    }
}

internal enum class MedicationUnit(val id: Int) {
    UNIT_KG(0),
    UNIT_L(1);

    companion object {
        fun create(value: Int): MedicationUnit {
            return values().firstOrNull { it.id == value }
                ?: throw IllegalArgumentException("Cannot find element for provided value.")
        }
    }
}
