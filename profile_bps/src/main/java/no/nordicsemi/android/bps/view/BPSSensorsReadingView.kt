package no.nordicsemi.android.bps.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.bps.R
import no.nordicsemi.android.bps.data.BPSData
import no.nordicsemi.android.theme.view.BatteryLevelView
import no.nordicsemi.android.theme.view.KeyValueField
import no.nordicsemi.android.theme.view.ScreenSection
import no.nordicsemi.android.theme.view.SectionTitle

@Composable
internal fun BPSSensorsReadingView(state: BPSData) {
    ScreenSection {
        Column {
            SectionTitle(resId = R.drawable.ic_records, title = stringResource(id = R.string.bps_records))
            Spacer(modifier = Modifier.height(16.dp))
            KeyValueField(stringResource(id = R.string.bps_systolic), state.displaySystolic())
            Spacer(modifier = Modifier.height(4.dp))
            KeyValueField(stringResource(id = R.string.bps_diastolic), state.displayDiastolic())
            Spacer(modifier = Modifier.height(4.dp))
            KeyValueField(stringResource(id = R.string.bps_mean), state.displayMeanArterialPressure())

            state.displayHeartRate()?.let {
                Spacer(modifier = Modifier.height(4.dp))
                KeyValueField(stringResource(id = R.string.bps_pulse), it)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    state.batteryLevel?.let {
        BatteryLevelView(it)
    }
}

@Preview
@Composable
private fun Preview() {
    BPSSensorsReadingView(BPSData())
}
