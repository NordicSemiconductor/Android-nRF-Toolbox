package no.nordicsemi.android.csc.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.csc.R
import no.nordicsemi.android.csc.viewmodel.CSCViewState
import no.nordicsemi.android.theme.NordicColors
import no.nordicsemi.android.theme.view.BatteryLevelView
import no.nordicsemi.android.theme.view.KeyValueField

@Composable
internal fun SensorsReadingView(state: CSCViewState) {
    Card(
        backgroundColor = NordicColors.NordicGray4.value(),
        shape = RoundedCornerShape(10.dp),
        elevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
    SensorsReadingView(CSCViewState())
}
