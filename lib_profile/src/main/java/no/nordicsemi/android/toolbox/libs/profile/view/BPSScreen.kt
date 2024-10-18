package no.nordicsemi.android.toolbox.libs.profile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.profile.data.bps.BloodPressureMeasurementData
import no.nordicsemi.android.toolbox.libs.profile.data.bps.IntermediateCuffPressureData
import no.nordicsemi.android.toolbox.libs.profile.data.service.BPSServiceData
import no.nordicsemi.android.ui.view.KeyValueField
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

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
                resId = R.drawable.ic_records,
                title = stringResource(id = R.string.bps_records)
            )

            serviceData.bloodPressureMeasurement?.let {
                BloodPressureView(it)
            }

            serviceData.intermediateCuffPressure?.displayHeartRate()?.let {
                KeyValueField(stringResource(id = R.string.bps_pulse), it)
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

@Composable
private fun BloodPressureView(state: BloodPressureMeasurementData) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyValueField(stringResource(id = R.string.bps_systolic), state.displaySystolic())
        KeyValueField(stringResource(id = R.string.bps_diastolic), state.displayDiastolic())
        KeyValueField(stringResource(id = R.string.bps_mean), state.displayMeanArterialPressure())
    }
}

@Composable
fun BloodPressureMeasurementData.displaySystolic(): String =
    stringResource(id = R.string.bps_blood_pressure, systolic)

@Composable
fun BloodPressureMeasurementData.displayDiastolic(): String =
    stringResource(id = R.string.bps_blood_pressure, diastolic)

@Composable
fun BloodPressureMeasurementData.displayMeanArterialPressure(): String =
    stringResource(id = R.string.bps_blood_pressure, meanArterialPressure)

@Composable
fun IntermediateCuffPressureData.displayHeartRate(): String? = pulseRate?.toString()

@Preview
@Composable
private fun Preview() {
    BPSScreen(BPSServiceData())
}