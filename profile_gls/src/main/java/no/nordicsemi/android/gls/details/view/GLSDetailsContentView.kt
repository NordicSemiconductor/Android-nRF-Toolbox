package no.nordicsemi.android.gls.details.view

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ble.common.profile.glucose.GlucoseMeasurementCallback
import no.nordicsemi.android.ble.common.profile.glucose.GlucoseMeasurementContextCallback
import no.nordicsemi.android.gls.R
import no.nordicsemi.android.gls.data.*
import no.nordicsemi.android.gls.main.view.toDisplayString
import java.util.*

@Composable
internal fun GLSDetailsContentView(record: GLSRecord) {
    Field(stringResource(id = R.string.gls_details_sequence_number), record.sequenceNumber.toString())

    record.time?.let {
        Field(stringResource(id = R.string.gls_details_date_and_time), stringResource(R.string.gls_timestamp, it))
        Spacer(modifier = Modifier.size(4.dp))
    }
    record.type?.let {
        Field(stringResource(id = R.string.gls_details_type), it.toDisplayString())
        Spacer(modifier = Modifier.size(4.dp))
    }
    record.sampleLocation?.let {
        Field(stringResource(id = R.string.gls_details_location), it.toDisplayString())
        Spacer(modifier = Modifier.size(4.dp))
    }

    Field(stringResource(id = R.string.gls_details_glucose_condensation_title), stringResource(id = R.string.gls_details_glucose_condensation_field, record.glucoseConcentration, record.unit.toDisplayString()))

    record.status?.let {
        Field(stringResource(id = R.string.gls_details_battery_low), it.deviceBatteryLow.toGLSStatus())
        Spacer(modifier = Modifier.size(4.dp))
        Field(stringResource(id = R.string.gls_details_sensor_malfunction), it.sensorMalfunction.toGLSStatus())
        Spacer(modifier = Modifier.size(4.dp))
        Field(stringResource(id = R.string.gls_details_insufficient_sample), it.sampleSizeInsufficient.toGLSStatus())
        Spacer(modifier = Modifier.size(4.dp))
        Field(stringResource(id = R.string.gls_details_strip_insertion_error), it.stripInsertionError.toGLSStatus())
        Spacer(modifier = Modifier.size(4.dp))
        Field(stringResource(id = R.string.gls_details_strip_type_incorrect), it.stripTypeIncorrect.toGLSStatus())
        Spacer(modifier = Modifier.size(4.dp))
        Field(stringResource(id = R.string.gls_details_sensor_result_too_high), it.sensorResultHigherThenDeviceCanProcess.toGLSStatus())
        Spacer(modifier = Modifier.size(4.dp))
        Field(stringResource(id = R.string.gls_details_sensor_result_too_low), it.sensorResultLowerThenDeviceCanProcess.toGLSStatus())
        Spacer(modifier = Modifier.size(4.dp))
        Field(stringResource(id = R.string.gls_details_temperature_too_high), it.sensorTemperatureTooHigh.toGLSStatus())
        Spacer(modifier = Modifier.size(4.dp))
        Field(stringResource(id = R.string.gls_details_temperature_too_low), it.sensorTemperatureTooLow.toGLSStatus())
        Spacer(modifier = Modifier.size(4.dp))
        Field(stringResource(id = R.string.gls_details_strip_pulled_too_soon), it.sensorReadInterrupted.toGLSStatus())
        Spacer(modifier = Modifier.size(4.dp))
        Field(stringResource(id = R.string.gls_details_general_device_fault), it.generalDeviceFault.toGLSStatus())
        Spacer(modifier = Modifier.size(4.dp))
        Field(stringResource(id = R.string.gls_details_time_fault), it.timeFault.toGLSStatus())
        Spacer(modifier = Modifier.size(4.dp))
    }

    record.context?.let {
        Field(stringResource(id = R.string.gls_context_title), stringResource(id = R.string.gls_available))
        Spacer(modifier = Modifier.size(4.dp))
        it.carbohydrate?.let {
            Field(stringResource(id = R.string.gls_context_carbohydrate), it.toDisplayString())
            Spacer(modifier = Modifier.size(4.dp))
        }
        it.meal?.let {
            Field(stringResource(id = R.string.gls_context_meal), it.toDisplayString())
            Spacer(modifier = Modifier.size(4.dp))
        }
        it.tester?.let {
            Field(stringResource(id = R.string.gls_context_tester), it.toDisplayString())
            Spacer(modifier = Modifier.size(4.dp))
        }
        it.health?.let {
            Field(stringResource(id = R.string.gls_context_health), it.toDisplayString())
            Spacer(modifier = Modifier.size(4.dp))
        }
        Field(stringResource(id = R.string.gls_context_exercise_title), stringResource(id = R.string.gls_context_exercise_field, it.exerciseDuration, it.exerciseIntensity))
        Spacer(modifier = Modifier.size(4.dp))

        val medicationField = String.format(stringResource(id = R.string.gls_context_medication_field), it.medicationQuantity, it.medicationUnit.toDisplayString(), it.medication?.toDisplayString())
        Field(stringResource(id = R.string.gls_context_medication_title), medicationField)

        Spacer(modifier = Modifier.size(4.dp))
        Field(stringResource(id = R.string.gls_context_hba1c_title), stringResource(id = R.string.gls_context_hba1c_field, it.HbA1c))
        Spacer(modifier = Modifier.size(4.dp))
    } ?: Field(stringResource(id = R.string.gls_context_title), stringResource(id = R.string.gls_unavailable))
}

@Composable
private fun GLSDetailsContentView() {
    val record = GLSRecord(
        sequenceNumber = 1,
        time = Calendar.getInstance(),
        glucoseConcentration = 12f,
        type = RecordType.ARTERIAL_PLASMA,
        status = GlucoseMeasurementCallback.GlucoseStatus(0x0004),
        unit = ConcentrationUnit.UNIT_KGPL,
        sampleLocation = SampleLocation.FINGER,
        context = MeasurementContext(
            sequenceNumber = 3,
            carbohydrate = GlucoseMeasurementContextCallback.Carbohydrate.BREAKFAST,
            carbohydrateAmount = 23f,
            meal = GlucoseMeasurementContextCallback.Meal.BEDTIME,
            tester = GlucoseMeasurementContextCallback.Tester.HEALTH_CARE_PROFESSIONAL,
            health = GlucoseMeasurementContextCallback.Health.MAJOR_HEALTH_ISSUES,
            exerciseDuration = 3,
            exerciseIntensity = 3,
            medication = GlucoseMeasurementContextCallback.Medication.INTERMEDIATE_ACTING_INSULIN,
            medicationQuantity = 4f,
            medicationUnit = MedicationUnit.UNIT_KG,
            HbA1c = 21f
        )
    )
    GLSDetailsContentView(record)
}
