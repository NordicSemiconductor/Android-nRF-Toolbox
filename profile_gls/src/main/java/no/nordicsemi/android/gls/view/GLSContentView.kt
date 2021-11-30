package no.nordicsemi.android.gls.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.gls.R
import no.nordicsemi.android.gls.data.GLSData
import no.nordicsemi.android.gls.viewmodel.DisconnectEvent
import no.nordicsemi.android.gls.viewmodel.GLSScreenViewEvent
import no.nordicsemi.android.gls.viewmodel.OnWorkingModeSelected
import no.nordicsemi.android.theme.view.BatteryLevelView
import no.nordicsemi.android.theme.view.ScreenSection

@Composable
internal fun GLSContentView(state: GLSData, onEvent: (GLSScreenViewEvent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        SettingsView(state, onEvent)

        Spacer(modifier = Modifier.height(16.dp))

        RecordsView(state)

        Spacer(modifier = Modifier.height(16.dp))

        BatteryLevelView(state.batteryLevel)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onEvent(DisconnectEvent) }
        ) {
            Text(text = stringResource(id = R.string.disconnect))
        }
    }
}

@Composable
private fun SettingsView(state: GLSData, onEvent: (GLSScreenViewEvent) -> Unit) {
    ScreenSection {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            state.modeItems().forEach {
                Button(onClick = { onEvent(OnWorkingModeSelected(it.unit)) }) {
                    Text(it.label)
                }
            }
        }
    }
}

@Composable
private fun RecordsView(state: GLSData) {
    ScreenSection {
        Column(modifier = Modifier.fillMaxWidth()) {
            state.records.forEach {
                Text(text = String.format("Glucose concentration: ", it.glucoseConcentration))
            }
        }
    }
}
