package no.nordicsemi.android.csc.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.csc.R
import no.nordicsemi.android.csc.data.CSCData
import no.nordicsemi.android.theme.view.ScreenSection
import no.nordicsemi.android.theme.view.SelectItemRadioGroup

@Composable
internal fun CSCContentView(state: CSCData, onEvent: (CSCViewEvent) -> Unit) {
    if (state.showDialog) {
        SelectWheelSizeDialog { onEvent(it) }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(state, onEvent)

        Spacer(modifier = Modifier.height(16.dp))

        SensorsReadingView(state = state)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onEvent(OnDisconnectButtonClick) }
        ) {
            Text(text = stringResource(id = R.string.disconnect))
        }
    }
}

@Composable
private fun SettingsSection(state: CSCData, onEvent: (CSCViewEvent) -> Unit) {
    ScreenSection {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WheelSizeView(state, onEvent)

            Spacer(modifier = Modifier.height(16.dp))

            SelectItemRadioGroup(state.selectedSpeedUnit, state.items()) {
                onEvent(OnSelectedSpeedUnitSelected(it.unit))
            }
        }
    }
}

@Preview
@Composable
private fun ConnectedPreview() {
    CSCContentView(CSCData()) { }
}
