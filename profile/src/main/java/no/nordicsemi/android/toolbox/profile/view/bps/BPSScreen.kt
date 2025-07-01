package no.nordicsemi.android.toolbox.profile.view.bps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.lib.profile.bps.BPMStatus
import no.nordicsemi.android.lib.profile.bps.BloodPressureFeatureData
import no.nordicsemi.android.lib.profile.bps.BloodPressureMeasurementData
import no.nordicsemi.android.lib.profile.bps.BloodPressureType
import no.nordicsemi.android.lib.profile.bps.IntermediateCuffPressureData
import no.nordicsemi.android.toolbox.lib.utils.Profile
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.BPSServiceData
import no.nordicsemi.android.ui.view.FeatureSupported
import no.nordicsemi.android.ui.view.KeyValueColumn
import no.nordicsemi.android.ui.view.KeyValueColumnReverse
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionRow
import no.nordicsemi.android.ui.view.SectionTitle
import no.nordicsemi.android.ui.view.dialog.toBooleanText
import java.util.Calendar

@Composable
internal fun BPSScreen(
    serviceData: BPSServiceData
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ScreenSection(modifier = Modifier.padding(bottom = 16.dp)) {
            SectionTitle(
                resId = R.drawable.ic_bps,
                title = stringResource(id = R.string.bps_title),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
            )
            serviceData.bloodPressureMeasurement?.let {
                BloodPressureView(it)
            }
            serviceData.intermediateCuffPressure?.displayHeartRate()?.let {
                HorizontalDivider()
                SectionRow {
                    KeyValueColumn(
                        stringResource(id = R.string.bps_pulse),
                        it,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            serviceData.bloodPressureFeature?.let {
                HorizontalDivider()
                BloodPressureFeatureView(it)
            }

            if (serviceData.intermediateCuffPressure == null &&
                serviceData.bloodPressureMeasurement == null &&
                serviceData.bloodPressureFeature == null
            ) {
                WaitingForMeasurementView()
            }
        }
    }
}

@Composable
internal fun WaitingForMeasurementView() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = stringResource(id = R.string.no_data_info_title),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(id = R.string.no_data_info),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WaitingForMeasurementViewPreview() {
    WaitingForMeasurementView()
}

@Composable
private fun BloodPressureFeatureView(it: BloodPressureFeatureData) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
    ) {
        if (it.bodyMovementDetection) {
            FeatureSupported(stringResource(id = R.string.body_movement_detected))
        }
        if (it.cuffFitDetection) {
            FeatureSupported(stringResource(id = R.string.cuff_fit_detected))
        }
        if (it.irregularPulseDetection) {
            FeatureSupported(stringResource(id = R.string.irregular_heart_rate_detected))
        }
        if (it.pulseRateRangeDetection) {
            FeatureSupported(stringResource(id = R.string.pulse_rate_detected))
        }
        if (it.measurementPositionDetection) {
            FeatureSupported(stringResource(id = R.string.measurement_position_detected))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BPSScreenPreview() {
    val sampleStatus = BPMStatus(
        bodyMovementDetected = true,
        cuffTooLose = false,
        irregularPulseDetected = true,
        pulseRateInRange = false,
        pulseRateExceedsUpperLimit = true,
        pulseRateIsLessThenLowerLimit = false,
        improperMeasurementPosition = true
    )

    val sampleData = BloodPressureMeasurementData(
        systolic = 12.0f,
        diastolic = 15.0f,
        meanArterialPressure = 10.0f,
        unit = BloodPressureType.UNIT_MMHG,
        pulseRate = 12.0f,
        userID = 15,
        status = sampleStatus,
        calendar = Calendar.getInstance()
    )
    BPSScreen(
        serviceData = BPSServiceData(
            profile = Profile.BPS,
            bloodPressureMeasurement = sampleData,
            intermediateCuffPressure = IntermediateCuffPressureData(
                unit = BloodPressureType.UNIT_MMHG,
                pulseRate = 12.0f,
                userID = 15,
                status = sampleStatus,
                calendar = Calendar.getInstance(),
                cuffPressure = 25.5f
            ),
            bloodPressureFeature = BloodPressureFeatureData(
                bodyMovementDetection = true,
                cuffFitDetection = true,
                irregularPulseDetection = true,
                pulseRateRangeDetection = true,
                measurementPositionDetection = true
            )
        )
    )
}

@Composable
private fun BloodPressureView(state: BloodPressureMeasurementData) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
    ) {
        SectionRow {
            KeyValueColumn(
                stringResource(id = R.string.bps_systolic),
                state.displaySystolic()
            )
            KeyValueColumnReverse(
                stringResource(id = R.string.bps_diastolic),
                state.displayDiastolic()
            )
        }
        SectionRow {
            KeyValueColumn(
                stringResource(id = R.string.bps_mean),
                state.displayMeanArterialPressure()
            )
            state.pulseRate?.let {
                KeyValueColumnReverse(
                    "Heart rate", state.displayPulseRate()
                )
            }
        }
        SectionRow {
            state.calendar?.let {
                stringResource(R.string.bps_timestamp, it)
            }?.let {
                KeyValueColumn(
                    "Date & Time",
                    it
                )
            }
        }
    }
    state.status?.let {
        HorizontalDivider()
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp)
        ) {
            if (it.bodyMovementDetected) {
                KeyValueColumn(
                    stringResource(id = R.string.body_movement_detected),
                    it.bodyMovementDetected.toBooleanText()
                )
            }
            if (it.irregularPulseDetected) {
                KeyValueColumn(
                    stringResource(id = R.string.irregular_heart_rate_detected),
                    it.irregularPulseDetected.toBooleanText()
                )
            }

            if (it.cuffTooLose) {
                KeyValueColumn(
                    "Cuff Too Lose",
                    it.cuffTooLose.toBooleanText()
                )
            }
            if (it.pulseRateExceedsUpperLimit) {
                KeyValueColumn(
                    stringResource(id = R.string.pulse_rate_higher_limit),
                    it.pulseRateExceedsUpperLimit.toBooleanText()
                )
            }
            if (it.pulseRateInRange) {
                KeyValueColumn(
                    stringResource(id = R.string.pulse_rate_detected),
                    it.pulseRateInRange.toBooleanText()
                )
            }
            if (it.improperMeasurementPosition) {
                KeyValueColumn(
                    stringResource(id = R.string.improper_measurement_position),
                    it.improperMeasurementPosition.toBooleanText()
                )
            }

            if (it.pulseRateIsLessThenLowerLimit) {
                KeyValueColumn(
                    stringResource(id = R.string.pulse_rate_lower_limit),
                    it.pulseRateIsLessThenLowerLimit.toBooleanText()
                )
            }
        }
    }
}

@Composable
fun BloodPressureMeasurementData.displaySystolic(): String =
    stringResource(
        id = R.string.bps_blood_pressure,
        systolic, displayUnit()
    )

@Composable
fun BloodPressureMeasurementData.displayDiastolic(): String =
    stringResource(
        id = R.string.bps_blood_pressure,
        diastolic, displayUnit()
    )

@Composable
fun BloodPressureMeasurementData.displayMeanArterialPressure(): String =
    stringResource(
        id = R.string.bps_blood_pressure,
        meanArterialPressure, displayUnit()
    )

@Composable
fun IntermediateCuffPressureData.displayHeartRate(): String = pulseRate?.toString() + " bpm"

@Composable
fun BloodPressureMeasurementData.displayPulseRate(): String = pulseRate?.toString() + " bpm"

@Composable
fun BloodPressureMeasurementData.displayUnit(): String =
    if (unit == BloodPressureType.UNIT_MMHG) "mmHg" else "kPA"
