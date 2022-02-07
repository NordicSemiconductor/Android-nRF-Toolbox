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
