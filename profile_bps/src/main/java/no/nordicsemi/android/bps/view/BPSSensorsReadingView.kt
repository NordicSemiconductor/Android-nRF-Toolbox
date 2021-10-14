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

@Composable
internal fun BPSSensorsReadingView(state: BPSData) {
    ScreenSection {
        Column {
            KeyValueField(stringResource(id = R.string.bps_systolic), state.displaySystolic())
            Spacer(modifier = Modifier.height(4.dp))
            KeyValueField(stringResource(id = R.string.bps_diastolic), state.displayDiastolic())
            Spacer(modifier = Modifier.height(4.dp))
            KeyValueField(stringResource(id = R.string.bps_mean), state.displayMeanArterialPressure())
            Spacer(modifier = Modifier.height(4.dp))
            KeyValueField(stringResource(id = R.string.bps_pulse), state.displayPulse())
            Spacer(modifier = Modifier.height(4.dp))
            KeyValueField(stringResource(id = R.string.bps_time_data), state.displayTimeData())
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    BatteryLevelView(state.batteryLevel)
}

@Preview
@Composable
private fun Preview() {
    BPSSensorsReadingView(BPSData())
}
