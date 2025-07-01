package no.nordicsemi.android.toolbox.profile.view.gls.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.lib.profile.gls.data.Carbohydrate
import no.nordicsemi.android.lib.profile.gls.data.ConcentrationUnit
import no.nordicsemi.android.lib.profile.gls.data.GLSMeasurementContext
import no.nordicsemi.android.lib.profile.gls.data.GLSRecord
import no.nordicsemi.android.lib.profile.gls.data.GlucoseStatus
import no.nordicsemi.android.lib.profile.gls.data.Health
import no.nordicsemi.android.lib.profile.gls.data.Meal
import no.nordicsemi.android.lib.profile.gls.data.Medication
import no.nordicsemi.android.lib.profile.gls.data.MedicationUnit
import no.nordicsemi.android.lib.profile.gls.data.RecordType
import no.nordicsemi.android.lib.profile.gls.data.SampleLocation
import no.nordicsemi.android.lib.profile.gls.data.Tester
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.view.gls.glucoseConcentrationDisplayValue
import no.nordicsemi.android.toolbox.profile.view.gls.toDisplayString
import no.nordicsemi.android.ui.view.KeyValueColumn
import no.nordicsemi.android.ui.view.KeyValueColumnReverse
import no.nordicsemi.android.ui.view.KeyValueField
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionRow
import no.nordicsemi.android.ui.view.dialog.toBooleanText
import java.util.Calendar

@Composable
internal fun GLSDetails(record: GLSRecord, context: GLSMeasurementContext?) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenSection {
            Column {
                SectionRow {
                    KeyValueColumn(
                        stringResource(id = R.string.gls_details_sequence_number),
                        record.sequenceNumber.toString()
                    )
                    record.time?.let {
                        KeyValueColumnReverse(
                            stringResource(id = R.string.gls_details_date_and_time),
                            stringResource(R.string.gls_timestamp, it)
                        )
                    }
                }

            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            SectionRow {
                record.type?.let {
                    KeyValueColumn(
                        stringResource(id = R.string.gls_details_type), it.toDisplayString()
                    )
                }
                record.sampleLocation?.let {
                    KeyValueColumnReverse(
                        stringResource(id = R.string.gls_details_location),
                        it.toDisplayString()
                    )
                }

            }
            SectionRow {
                record.glucoseConcentration?.let { glucoseConcentration ->
                    record.unit?.let { unit ->
                        KeyValueColumn(
                            stringResource(id = R.string.gls_details_glucose_condensation_title),
                            glucoseConcentrationDisplayValue(glucoseConcentration, unit),
                            keyStyle = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            record.status?.let {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    "Glucose status",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                SectionRow {
                    KeyValueColumn(
                        stringResource(id = R.string.gls_details_battery_low),
                        it.deviceBatteryLow.toBooleanText(),
                        verticalSpacing = 4.dp
                    )
                    KeyValueColumnReverse(
                        stringResource(id = R.string.gls_details_sensor_malfunction),
                        it.sensorMalfunction.toBooleanText(),
                        verticalSpacing = 4.dp
                    )
                }
                SectionRow {
                    KeyValueColumn(
                        stringResource(id = R.string.gls_details_insufficient_sample),
                        it.sampleSizeInsufficient.toBooleanText(),
                        verticalSpacing = 4.dp
                    )
                    KeyValueColumnReverse(
                        stringResource(id = R.string.gls_details_strip_insertion_error),
                        it.stripInsertionError.toBooleanText(),
                        verticalSpacing = 4.dp
                    )
                }
                SectionRow {
                    KeyValueColumn(
                        stringResource(id = R.string.gls_details_strip_type_incorrect),
                        it.stripTypeIncorrect.toBooleanText(),
                        verticalSpacing = 4.dp
                    )
                    KeyValueColumnReverse(
                        stringResource(id = R.string.gls_details_sensor_result_too_high),
                        it.sensorResultHigherThenDeviceCanProcess.toBooleanText(),
                        verticalSpacing = 4.dp
                    )
                }

                SectionRow {
                    KeyValueColumn(
                        stringResource(id = R.string.gls_details_sensor_result_too_low),
                        it.sensorResultLowerThenDeviceCanProcess.toBooleanText(),
                        verticalSpacing = 4.dp
                    )
                    KeyValueColumnReverse(
                        stringResource(id = R.string.gls_details_temperature_too_high),
                        it.sensorTemperatureTooHigh.toBooleanText(),
                        verticalSpacing = 4.dp
                    )
                }

                SectionRow {
                    KeyValueColumn(
                        stringResource(id = R.string.gls_details_temperature_too_low),
                        it.sensorTemperatureTooLow.toBooleanText(),
                        verticalSpacing = 4.dp
                    )
                    KeyValueColumnReverse(
                        stringResource(id = R.string.gls_details_strip_pulled_too_soon),
                        it.sensorReadInterrupted.toBooleanText(),
                        verticalSpacing = 4.dp
                    )
                }

                SectionRow {
                    KeyValueColumn(
                        stringResource(id = R.string.gls_details_general_device_fault),
                        it.generalDeviceFault.toBooleanText(),
                        verticalSpacing = 4.dp
                    )
                    KeyValueColumnReverse(
                        stringResource(id = R.string.gls_details_time_fault),
                        it.timeFault.toBooleanText(),
                        verticalSpacing = 4.dp
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            context?.let { glsMeasurementContext ->
                Text(
                    stringResource(id = R.string.gls_context_title),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                SectionRow {
                    KeyValueColumn(
                        stringResource(id = R.string.gls_details_sequence_number),
                        glsMeasurementContext.sequenceNumber.toString(),
                        verticalSpacing = 4.dp
                    )
                    glsMeasurementContext.carbohydrate?.let {
                        val carbohydrateAmount = glsMeasurementContext.carbohydrateAmount
                        KeyValueColumnReverse(
                            stringResource(id = R.string.gls_context_carbohydrate),
                            it.toDisplayString() + " ($carbohydrateAmount g)",
                            verticalSpacing = 4.dp
                        )
                    }
                }
                SectionRow {
                    glsMeasurementContext.meal?.let {
                        KeyValueColumn(
                            stringResource(id = R.string.gls_context_meal),
                            it.toDisplayString(),
                            verticalSpacing = 4.dp
                        )
                    }
                    glsMeasurementContext.tester?.let {
                        KeyValueColumnReverse(
                            stringResource(id = R.string.gls_context_tester),
                            it.toDisplayString(),
                            verticalSpacing = 4.dp
                        )
                    }
                }
                SectionRow {
                    glsMeasurementContext.health?.let {
                        KeyValueColumn(
                            stringResource(id = R.string.gls_context_health),
                            it.toDisplayString(),
                            verticalSpacing = 4.dp
                        )
                    }
                    glsMeasurementContext.exerciseDuration?.let { duration ->
                        glsMeasurementContext.exerciseIntensity?.let { exerciseIntensity ->
                            KeyValueColumnReverse(
                                stringResource(id = R.string.gls_context_exercise_title),
                                stringResource(
                                    id = R.string.gls_context_exercise_field,
                                    getExerciseDuration(duration),
                                    exerciseIntensity
                                ),
                                verticalSpacing = 4.dp
                            )
                        }
                    }
                }
                SectionRow {
                    glsMeasurementContext.medicationUnit?.let { medicationUnit ->
                        val medicationField = String.format(
                            stringResource(id = R.string.gls_context_medication_field),
                            glsMeasurementContext.medication?.toDisplayString(),
                            glsMeasurementContext.medicationQuantity,
                            medicationUnit.toDisplayString()
                        )
                        KeyValueColumn(
                            stringResource(id = R.string.gls_context_medication_title),
                            medicationField,
                            verticalSpacing = 4.dp
                        )
                    }

                    glsMeasurementContext.HbA1c?.let { hbA1c ->
                        KeyValueColumnReverse(
                            stringResource(id = R.string.gls_context_hba1c_title),
                            stringResource(id = R.string.gls_context_hba1c_field, hbA1c),
                            verticalSpacing = 4.dp
                        )
                    }
                }

            } ?: KeyValueField(
                stringResource(id = R.string.gls_context_title),
                stringResource(id = R.string.gls_unavailable)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GlsDetailsPreview() {
    GLSDetails(
        record = GLSRecord(
            sequenceNumber = 1,
            time = Calendar.getInstance(),
            glucoseConcentration = 0.5f,
            unit = ConcentrationUnit.UNIT_KGPL,
            type = RecordType.VENOUS_PLASMA,
            status = GlucoseStatus(212),
            sampleLocation = SampleLocation.FINGER,
            contextInformationFollows = true
        ),
        context = GLSMeasurementContext(
            sequenceNumber = 20,
            carbohydrate = Carbohydrate.LUNCH,
            carbohydrateAmount = 12.5f,
            meal = Meal.CASUAL,
            tester = Tester.SELF,
            health = Health.NO_HEALTH_ISSUES,
            exerciseDuration = 4520, // 1 hour, 15 minutes and 20 seconds
            exerciseIntensity = 1,
            medication = Medication.PRE_MIXED_INSULIN,
            medicationQuantity = .5f,
            medicationUnit = MedicationUnit.UNIT_KG,
            HbA1c = 0.5f
        )
    )
}