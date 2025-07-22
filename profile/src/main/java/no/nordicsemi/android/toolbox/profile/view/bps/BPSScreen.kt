package no.nordicsemi.android.toolbox.profile.view.bps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.lib.profile.bps.BloodPressureFeatureData
import no.nordicsemi.android.lib.profile.bps.BloodPressureMeasurementData
import no.nordicsemi.android.lib.profile.bps.BloodPressureType
import no.nordicsemi.android.lib.profile.bps.IntermediateCuffPressureData
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.viewmodel.BPSViewModel
import no.nordicsemi.android.ui.view.FeatureSupported
import no.nordicsemi.android.ui.view.KeyValueColumn
import no.nordicsemi.android.ui.view.KeyValueColumnReverse
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionRow
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun BPSScreen() {
    val bpsViewModel = hiltViewModel<BPSViewModel>()
    val serviceData by bpsViewModel.bpsServiceState.collectAsStateWithLifecycle()

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
                        verticalSpacing = 4.dp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            serviceData.bloodPressureFeature?.let {
                HorizontalDivider()
                Text(
                    "Blood pressure features",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
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
internal fun BloodPressureFeatureView(it: BloodPressureFeatureData) {
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

@Composable
internal fun BloodPressureView(state: BloodPressureMeasurementData) {
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
        Text(
            "BPM status",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp)
        ) {
            if (it.bodyMovementDetected) {
                FeatureSupported(stringResource(id = R.string.body_movement_detected))

            }
            if (it.irregularPulseDetected) {
                FeatureSupported(stringResource(id = R.string.irregular_heart_rate_detected))
            }

            if (it.cuffTooLose) {
                FeatureSupported("Cuff Too Lose")
            }
            if (it.pulseRateExceedsUpperLimit) {
                FeatureSupported(stringResource(id = R.string.pulse_rate_higher_limit))
            }
            if (it.pulseRateInRange) {
                FeatureSupported(stringResource(id = R.string.pulse_rate_detected))
            }
            if (it.improperMeasurementPosition) {
                FeatureSupported(stringResource(id = R.string.improper_measurement_position))
            }

            if (it.pulseRateIsLessThenLowerLimit) {
                FeatureSupported(stringResource(id = R.string.pulse_rate_lower_limit))
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
