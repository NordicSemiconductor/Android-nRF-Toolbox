package no.nordicsemi.android.csc.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.csc.R
import no.nordicsemi.android.csc.data.CSCData
import no.nordicsemi.android.theme.NordicColors

@Composable
internal fun ContentView(state: CSCData, onEvent: (CSCViewEvent) -> Unit) {
    if (state.showDialog) {
        SelectWheelSizeDialog { onEvent(it) }
    }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SettingsSection(state, onEvent)

        Spacer(modifier = Modifier.height(16.dp))

        SensorsReadingView(state = state)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
            onClick = { onEvent(OnDisconnectButtonClick) }
        ) {
            Text(text = stringResource(id = R.string.disconnect))
        }
    }
}

@Composable
private fun SettingsSection(state: CSCData, onEvent: (CSCViewEvent) -> Unit) {
    Card(
        backgroundColor = NordicColors.NordicGray4.value(),
        shape = RoundedCornerShape(10.dp),
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WheelSizeView(state, onEvent)

            Spacer(modifier = Modifier.height(16.dp))

            SpeedUnitRadioGroup(state.selectedSpeedUnit) { onEvent(it) }
        }
    }
}

@Preview
@Composable
private fun ConnectedPreview() {
    ContentView(CSCData()) { }
}
