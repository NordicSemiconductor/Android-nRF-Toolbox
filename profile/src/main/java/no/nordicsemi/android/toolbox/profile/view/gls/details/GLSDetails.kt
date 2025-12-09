package no.nordicsemi.android.toolbox.profile.view.gls.details

import androidx.compose.foundation.layout.Arrangement
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
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.parser.gls.data.Carbohydrate
import no.nordicsemi.android.toolbox.profile.parser.gls.data.ConcentrationUnit
import no.nordicsemi.android.toolbox.profile.parser.gls.data.GLSMeasurementContext
import no.nordicsemi.android.toolbox.profile.parser.gls.data.GLSRecord
import no.nordicsemi.android.toolbox.profile.parser.gls.data.GlucoseStatus
import no.nordicsemi.android.toolbox.profile.parser.gls.data.Health
import no.nordicsemi.android.toolbox.profile.parser.gls.data.Meal
import no.nordicsemi.android.toolbox.profile.parser.gls.data.Medication
import no.nordicsemi.android.toolbox.profile.parser.gls.data.MedicationUnit
import no.nordicsemi.android.toolbox.profile.parser.gls.data.RecordType
import no.nordicsemi.android.toolbox.profile.parser.gls.data.SampleLocation
import no.nordicsemi.android.toolbox.profile.parser.gls.data.Tester
import no.nordicsemi.android.toolbox.profile.view.gls.glucoseConcentrationDisplayValue
import no.nordicsemi.android.toolbox.profile.view.gls.toDisplayString
import no.nordicsemi.android.ui.view.KeyValueColumn
import no.nordicsemi.android.ui.view.KeyValueColumnReverse
import no.nordicsemi.android.ui.view.KeyValueField
import no.nordicsemi.android.ui.view.SectionRow
import no.nordicsemi.android.ui.view.SubsectionTitle
import no.nordicsemi.android.ui.view.dialog.toBooleanText
import java.util.Calendar

@Composable
internal fun GLSDetails(record: GLSRecord, context: GLSMeasurementContext?) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionRow {
            KeyValueColumn(
                key = stringResource(id = R.string.gls_details_sequence_number),
                value = record.sequenceNumber.toString()
            )
            record.time?.let {
                KeyValueColumnReverse(
                    key = stringResource(id = R.string.gls_details_date_and_time),
                    value = stringResource(R.string.gls_timestamp, it)
                )
            }
        }

        SectionRow {
            record.type?.let {
                KeyValueColumn(
                    key = stringResource(id = R.string.gls_details_type),
                    value = it.toDisplayString()
                )
            }
            record.sampleLocation?.let {
                KeyValueColumnReverse(
                    key = stringResource(id = R.string.gls_details_location),
                    value = it.toDisplayString()
                )
            }
        }
        record.glucoseConcentration?.let { glucoseConcentration ->
            record.unit?.let { unit ->
                KeyValueColumn(
                    key = stringResource(id = R.string.gls_details_glucose_condensation_title),
                    value = glucoseConcentrationDisplayValue(glucoseConcentration, unit),
                )
            }
        }

        record.status?.let {
            SubsectionTitle(
                text = stringResource(R.string.gls_details_status_title)
            )

            SectionRow {
                KeyValueColumn(
                    key = stringResource(id = R.string.gls_details_battery_low),
                    value = it.deviceBatteryLow.toBooleanText(),
                )
                KeyValueColumnReverse(
                    key = stringResource(id = R.string.gls_details_sensor_malfunction),
                    value = it.sensorMalfunction.toBooleanText(),
                )
            }
            SectionRow {
                KeyValueColumn(
                    key = stringResource(id = R.string.gls_details_insufficient_sample),
                    value = it.sampleSizeInsufficient.toBooleanText(),
                )
                KeyValueColumnReverse(
                    key = stringResource(id = R.string.gls_details_strip_insertion_error),
                    value = it.stripInsertionError.toBooleanText(),
                )
            }
            SectionRow {
                KeyValueColumn(
                    key = stringResource(id = R.string.gls_details_strip_type_incorrect),
                    value = it.stripTypeIncorrect.toBooleanText(),
                )
                KeyValueColumnReverse(
                    key = stringResource(id = R.string.gls_details_sensor_result_too_high),
                    value = it.sensorResultHigherThenDeviceCanProcess.toBooleanText(),
                )
            }
            SectionRow {
                KeyValueColumn(
                    key = stringResource(id = R.string.gls_details_sensor_result_too_low),
                    value = it.sensorResultLowerThenDeviceCanProcess.toBooleanText(),
                )
                KeyValueColumnReverse(
                    key = stringResource(id = R.string.gls_details_temperature_too_high),
                    value = it.sensorTemperatureTooHigh.toBooleanText(),
                )
            }
            SectionRow {
                KeyValueColumn(
                    key = stringResource(id = R.string.gls_details_temperature_too_low),
                    value = it.sensorTemperatureTooLow.toBooleanText(),
                )
                KeyValueColumnReverse(
                    key = stringResource(id = R.string.gls_details_strip_pulled_too_soon),
                    value = it.sensorReadInterrupted.toBooleanText(),
                )
            }
            SectionRow {
                KeyValueColumn(
                    key = stringResource(id = R.string.gls_details_general_device_fault),
                    value = it.generalDeviceFault.toBooleanText(),
                )
                KeyValueColumnReverse(
                    key = stringResource(id = R.string.gls_details_time_fault),
                    value = it.timeFault.toBooleanText(),
                )
            }
        }

        context?.let { glsMeasurementContext ->
            SubsectionTitle(
                text = stringResource(id = R.string.gls_context_title)
            )

            SectionRow {
                KeyValueColumn(
                    key = stringResource(id = R.string.gls_details_sequence_number),
                    value = glsMeasurementContext.sequenceNumber.toString(),
                )
                glsMeasurementContext.carbohydrate?.let {
                    val carbohydrateAmount = glsMeasurementContext.carbohydrateAmount
                    KeyValueColumnReverse(
                        key = stringResource(id = R.string.gls_context_carbohydrate),
                        value = it.toDisplayString() + " ($carbohydrateAmount g)",
                    )
                }
            }
            SectionRow {
                glsMeasurementContext.meal?.let {
                    KeyValueColumn(
                        key = stringResource(id = R.string.gls_context_meal),
                        value = it.toDisplayString(),
                    )
                }
                glsMeasurementContext.tester?.let {
                    KeyValueColumnReverse(
                        key = stringResource(id = R.string.gls_context_tester),
                        value = it.toDisplayString(),
                    )
                }
            }
            SectionRow {
                glsMeasurementContext.health?.let {
                    KeyValueColumn(
                        key = stringResource(id = R.string.gls_context_health),
                        value = it.toDisplayString(),
                    )
                }
                glsMeasurementContext.exerciseDuration?.let { duration ->
                    glsMeasurementContext.exerciseIntensity?.let { exerciseIntensity ->
                        KeyValueColumnReverse(
                            key = stringResource(id = R.string.gls_context_exercise_title),
                            value = stringResource(
                                id = R.string.gls_context_exercise_field,
                                getExerciseDuration(duration),
                                exerciseIntensity
                            ),
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
                        key = stringResource(id = R.string.gls_context_medication_title),
                        value = medicationField,
                    )
                }

                glsMeasurementContext.HbA1c?.let { hbA1c ->
                    KeyValueColumnReverse(
                        key = stringResource(id = R.string.gls_context_hba1c_title),
                        value = stringResource(id = R.string.gls_context_hba1c_field, hbA1c),
                    )
                }
            }
        } ?: KeyValueField(
            key = stringResource(id = R.string.gls_context_title),
            value = stringResource(id = R.string.gls_unavailable)
        )
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