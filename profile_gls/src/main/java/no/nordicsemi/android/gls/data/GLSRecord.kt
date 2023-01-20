/*
 * Copyright (c) 2022, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.nordicsemi.android.gls.data

import no.nordicsemi.android.ble.common.profile.glucose.GlucoseMeasurementCallback.GlucoseStatus
import no.nordicsemi.android.ble.common.profile.glucose.GlucoseMeasurementContextCallback.*
import java.util.*

internal data class GLSRecord(
    val sequenceNumber: Int = 0,
    val time: Calendar? = null,
    val glucoseConcentration: Float = 0f,
    val unit: ConcentrationUnit = ConcentrationUnit.UNIT_KGPL,
    val type: RecordType? = null,
    val status: GlucoseStatus? = null,
    val sampleLocation: SampleLocation? = null,
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
    val sequenceNumber: Int = 0,
    val carbohydrate: Carbohydrate? = null,
    val carbohydrateAmount: Float = 0f,
    val meal: Meal? = null,
    val tester: Tester? = null,
    val health: Health? = null,
    val exerciseDuration: Int = 0,
    val exerciseIntensity: Int = 0,
    val medication: Medication?,
    val medicationQuantity: Float = 0f,
    val medicationUnit: MedicationUnit = MedicationUnit.UNIT_KG,
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

internal enum class SampleLocation(val id: Int) {
    FINGER(1),
    AST(2),
    EARLOBE(3),
    CONTROL_SOLUTION(4),
    NOT_AVAILABLE(15);

    companion object {
        fun createOrNull(value: Int?): SampleLocation? {
            return values().firstOrNull { it.id == value }
        }
    }
}
