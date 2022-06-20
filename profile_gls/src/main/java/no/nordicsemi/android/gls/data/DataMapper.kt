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

import no.nordicsemi.android.ble.common.callback.glucose.GlucoseMeasurementContextResponse
import no.nordicsemi.android.ble.common.callback.glucose.GlucoseMeasurementResponse

internal fun GlucoseMeasurementResponse.toRecord(): GLSRecord {
    return this.let {
        GLSRecord(
            sequenceNumber = it.sequenceNumber,
            time = it.time,
            glucoseConcentration = it.glucoseConcentration ?: 0f,
            unit = it.unit?.let { ConcentrationUnit.create(it) }
                ?: ConcentrationUnit.UNIT_KGPL,
            type = RecordType.createOrNull(it.type),
            sampleLocation = SampleLocation.createOrNull(it.sampleLocation),
            status = it.status
        )
    }
}

internal fun GlucoseMeasurementContextResponse.toMeasurementContext(): MeasurementContext {
    return this.let {
        MeasurementContext(
            sequenceNumber = it.sequenceNumber,
            carbohydrate = it.carbohydrate,
            carbohydrateAmount = it.carbohydrateAmount ?: 0f,
            meal = it.meal,
            tester = it.tester,
            health = it.health,
            exerciseDuration = it.exerciseDuration ?: 0,
            exerciseIntensity = it.exerciseIntensity ?: 0,
            medication = it.medication,
            medicationQuantity = it.medicationAmount ?: 0f,
            medicationUnit = it.medicationUnit?.let { MedicationUnit.create(it) }
                ?: MedicationUnit.UNIT_KG,
            HbA1c = it.hbA1c ?: 0f
        )
    }
}

internal fun GLSRecord.copyWithNewContext(response: GlucoseMeasurementContextResponse): GLSRecord {
    return copy(context = context)
}
