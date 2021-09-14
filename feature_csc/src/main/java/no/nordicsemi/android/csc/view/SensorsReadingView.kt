package no.nordicsemi.android.csc.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.csc.R
import no.nordicsemi.android.theme.Background

@Composable
internal fun SensorsReadingView(state: CSCViewConnectedState) {
    Column {
        Column(modifier = Background.whiteRoundedCorners()) {
            KeyValueField(stringResource(id = R.string.scs_field_speed), state.displaySpeed())
            KeyValueField(stringResource(id = R.string.scs_field_cadence), state.displayCadence())
            KeyValueField(stringResource(id = R.string.scs_field_distance), state.displayDistance())
            KeyValueField(
                stringResource(id = R.string.scs_field_total_distance),
                state.displayTotalDistance()
            )
            KeyValueField(stringResource(id = R.string.scs_field_gear_ratio), state.displaySpeed())
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Background.whiteRoundedCorners()) {
            KeyValueField(stringResource(id = R.string.scs_field_battery), state.displayBatteryLever())
        }
    }
}

@Composable
private fun KeyValueField(key: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = key)
        Text(text = value)
    }
}

@Preview
@Composable
private fun Preview() {
    SensorsReadingView(CSCViewConnectedState())
}
