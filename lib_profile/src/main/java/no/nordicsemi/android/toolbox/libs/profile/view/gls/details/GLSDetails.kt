package no.nordicsemi.android.toolbox.libs.profile.view.gls.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.Carbohydrate
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.ConcentrationUnit
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSMeasurementContext
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSRecord
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GlucoseStatus
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.Health
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.Meal
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.Medication
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.MedicationUnit
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RecordType
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.SampleLocation
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.Tester
import no.nordicsemi.android.toolbox.libs.profile.data.toDisplayString
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
                            stringResource(
                                id = R.string.gls_details_glucose_condensation_field,
                                glucoseConcentration,
                                unit.toDisplayString()
                            )
                        )
                    }
                }
            }

            record.status?.let {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                SectionRow {
                    KeyValueColumn(
                        stringResource(id = R.string.gls_details_battery_low),
                        it.deviceBatteryLow.toBooleanText()
                    )
                    KeyValueColumnReverse(
                        stringResource(id = R.string.gls_details_sensor_malfunction),
                        it.sensorMalfunction.toBooleanText()
                    )
                }
                SectionRow {
                    KeyValueColumn(
                        stringResource(id = R.string.gls_details_insufficient_sample),
                        it.sampleSizeInsufficient.toBooleanText()
                    )
                    KeyValueColumnReverse(
                        stringResource(id = R.string.gls_details_strip_insertion_error),
                        it.stripInsertionError.toBooleanText()
                    )
                }
                SectionRow {
                    KeyValueColumn(
                        stringResource(id = R.string.gls_details_strip_type_incorrect),
                        it.stripTypeIncorrect.toBooleanText()
                    )
                    KeyValueColumnReverse(
                        stringResource(id = R.string.gls_details_sensor_result_too_high),
                        it.sensorResultHigherThenDeviceCanProcess.toBooleanText()
                    )
                }

                SectionRow {
                    KeyValueColumn(
                        stringResource(id = R.string.gls_details_sensor_result_too_low),
                        it.sensorResultLowerThenDeviceCanProcess.toBooleanText()
                    )
                    KeyValueColumnReverse(
                        stringResource(id = R.string.gls_details_temperature_too_high),
                        it.sensorTemperatureTooHigh.toBooleanText()
                    )
                }

                SectionRow {
                    KeyValueColumn(
                        stringResource(id = R.string.gls_details_temperature_too_low),
                        it.sensorTemperatureTooLow.toBooleanText()
                    )
                    KeyValueColumnReverse(
                        stringResource(id = R.string.gls_details_strip_pulled_too_soon),
                        it.sensorReadInterrupted.toBooleanText()
                    )
                }

                SectionRow {
                    KeyValueColumn(
                        stringResource(id = R.string.gls_details_general_device_fault),
                        it.generalDeviceFault.toBooleanText()
                    )
                    KeyValueColumnReverse(
                        stringResource(id = R.string.gls_details_time_fault),
                        it.timeFault.toBooleanText()
                    )
                }
            }

            context?.let { glsMeasurementContext ->
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                SectionRow {
                    KeyValueColumn(
                        stringResource(id = R.string.gls_context_title),
                        stringResource(id = R.string.gls_available)
                    )
                    glsMeasurementContext.carbohydrate?.let {
                        KeyValueColumnReverse(
                            stringResource(id = R.string.gls_context_carbohydrate),
                            it.toDisplayString()
                        )
                    }
                }
                SectionRow {
                    glsMeasurementContext.meal?.let {
                        KeyValueColumn(
                            stringResource(id = R.string.gls_context_meal),
                            it.toDisplayString()
                        )
                    }
                    glsMeasurementContext.tester?.let {
                        KeyValueColumnReverse(
                            stringResource(id = R.string.gls_context_tester),
                            it.toDisplayString()
                        )
                    }
                }
                SectionRow {
                    glsMeasurementContext.health?.let {
                        KeyValueColumn(
                            stringResource(id = R.string.gls_context_health),
                            it.toDisplayString()
                        )
                    }
                    glsMeasurementContext.exerciseDuration?.let { exerciseDuration ->
                        glsMeasurementContext.exerciseIntensity?.let { exerciseIntensity ->
                            KeyValueColumnReverse(
                                stringResource(id = R.string.gls_context_exercise_title),
                                stringResource(
                                    id = R.string.gls_context_exercise_field,
                                    exerciseDuration,
                                    exerciseIntensity
                                )
                            )
                        }
                    }
                }

                SectionRow {
                    glsMeasurementContext.medicationUnit?.let { medicationUnit ->
                        val medicationField = String.format(
                            stringResource(id = R.string.gls_context_medication_field),
                            glsMeasurementContext.medicationQuantity,
                            medicationUnit.toDisplayString(),
                            glsMeasurementContext.medication?.toDisplayString()
                        )
                        KeyValueColumn(
                            stringResource(id = R.string.gls_context_medication_title),
                            medicationField
                        )
                    }

                    glsMeasurementContext.HbA1c?.let { hbA1c ->
                        KeyValueColumnReverse(
                            stringResource(id = R.string.gls_context_hba1c_title),
                            stringResource(id = R.string.gls_context_hba1c_field, hbA1c)
                        )
                    }
                }
            } ?: Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                KeyValueField(
                    stringResource(id = R.string.gls_context_title),
                    stringResource(id = R.string.gls_unavailable)
                )
            }
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
            exerciseDuration = 2,
            exerciseIntensity = 1,
            medication = Medication.PRE_MIXED_INSULIN,
            medicationQuantity = .5f,
            medicationUnit = MedicationUnit.UNIT_MG,
            HbA1c = 0.5f
        )
    )
}