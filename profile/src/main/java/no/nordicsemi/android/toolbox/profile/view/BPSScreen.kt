package no.nordicsemi.android.toolbox.profile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.Profile
import no.nordicsemi.android.toolbox.profile.data.BPSServiceData
import no.nordicsemi.android.lib.profile.bps.BPMStatus
import no.nordicsemi.android.lib.profile.bps.BloodPressureMeasurementData
import no.nordicsemi.android.lib.profile.bps.BloodPressureType
import no.nordicsemi.android.lib.profile.bps.IntermediateCuffPressureData
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
        ScreenSection {
            SectionTitle(
                resId = R.drawable.ic_bps,
                title = stringResource(id = R.string.bps_title)
            )

            serviceData.bloodPressureMeasurement?.let {
                BloodPressureView(it)
            }

            serviceData.intermediateCuffPressure?.displayHeartRate()?.let {
                HorizontalDivider()
                SectionRow {
                    KeyValueColumn(stringResource(id = R.string.bps_pulse), it)
                }
            }

            if (serviceData.intermediateCuffPressure == null && serviceData.bloodPressureMeasurement == null) {
                Text(
                    stringResource(id = R.string.no_data_info),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
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
        )
    )
}

@Composable
private fun BloodPressureView(state: BloodPressureMeasurementData) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    "Pulse", state.displayPulseRate()
                )
            }
        }
        SectionRow {
            state.calendar?.let {
                stringResource(R.string.bps_timestamp, it)
            }?.let {
                KeyValueColumn(
                    "Date/Time",
                    it
                )
            }
            state.status?.bodyMovementDetected?.let {
                KeyValueColumnReverse(
                    "Body movement detected",
                    it.toBooleanText()
                )
            }
        }

        state.status?.irregularPulseDetected?.let {
            KeyValueColumn(
                "Irregular pulse detected",
                it.toBooleanText()
            )
        }
        state.status?.cuffTooLose?.let {
            KeyValueColumn(
                "Cuff Too Lose",
                it.toBooleanText()
            )
        }


        state.status?.pulseRateExceedsUpperLimit?.let {
            KeyValueColumn(
                "Pulse rate exceeds upper limit",
                it.toBooleanText()
            )
        }
        state.status?.pulseRateInRange?.let {
            KeyValueColumn(
                "Pulse rate in range",
                it.toBooleanText()
            )
        }
        state.status?.improperMeasurementPosition?.let {
            KeyValueColumn(
                "Improper measurement position",
                it.toBooleanText()
            )
        }

        state.status?.pulseRateIsLessThenLowerLimit?.let {
            KeyValueColumn(
                "Pulse rate less than lower limit",
                it.toBooleanText()
            )
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
    if (unit == BloodPressureType.UNIT_MMHG) "mmGH" else "kPA"
