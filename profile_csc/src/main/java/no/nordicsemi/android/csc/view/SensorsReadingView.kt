package no.nordicsemi.android.csc.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.csc.R
import no.nordicsemi.android.csc.data.CSCData
import no.nordicsemi.android.theme.view.BatteryLevelView
import no.nordicsemi.android.theme.view.KeyValueField
import no.nordicsemi.android.theme.view.ScreenSection

@Composable
internal fun SensorsReadingView(state: CSCData) {
    ScreenSection {
        Column {
            KeyValueField(stringResource(id = R.string.scs_field_speed), state.displaySpeed())
            Spacer(modifier = Modifier.height(4.dp))
            KeyValueField(stringResource(id = R.string.scs_field_cadence), state.displayCadence())
            Spacer(modifier = Modifier.height(4.dp))
            KeyValueField(stringResource(id = R.string.scs_field_distance), state.displayDistance())
            Spacer(modifier = Modifier.height(4.dp))
            KeyValueField(
                stringResource(id = R.string.scs_field_total_distance),
                state.displayTotalDistance()
            )
            Spacer(modifier = Modifier.height(4.dp))
            KeyValueField(stringResource(id = R.string.scs_field_gear_ratio), state.displayGearRatio())
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    BatteryLevelView(state.batteryLevel)
}

@Preview
@Composable
private fun Preview() {
    SensorsReadingView(CSCData())
}
