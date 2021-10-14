package no.nordicsemi.android.rscs.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.rscs.R
import no.nordicsemi.android.rscs.data.RSCSData
import no.nordicsemi.android.theme.view.BatteryLevelView
import no.nordicsemi.android.theme.view.KeyValueField
import no.nordicsemi.android.theme.view.ScreenSection

@Composable
internal fun SensorsReadingView(state: RSCSData) {
    ScreenSection {
        Column {
            KeyValueField(stringResource(id = R.string.rscs_activity), state.displayActivity())
            Spacer(modifier = Modifier.height(4.dp))
            KeyValueField(stringResource(id = R.string.rscs_pace), state.displayPace())
            Spacer(modifier = Modifier.height(4.dp))
            KeyValueField(stringResource(id = R.string.rscs_cadence), state.displayCadence())
            Spacer(modifier = Modifier.height(4.dp))
            KeyValueField(
                stringResource(id = R.string.rscs_number_of_steps),
                state.displayNumberOfSteps()
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    BatteryLevelView(state.batteryLevel)
}

@Preview
@Composable
private fun Preview() {
    SensorsReadingView(RSCSData())
}
