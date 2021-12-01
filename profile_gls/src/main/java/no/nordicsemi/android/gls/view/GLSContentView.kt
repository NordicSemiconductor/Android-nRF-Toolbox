package no.nordicsemi.android.gls.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.gls.R
import no.nordicsemi.android.gls.data.GLSData
import no.nordicsemi.android.gls.data.GLSRecord
import no.nordicsemi.android.gls.viewmodel.DisconnectEvent
import no.nordicsemi.android.gls.viewmodel.GLSScreenViewEvent
import no.nordicsemi.android.gls.viewmodel.OnWorkingModeSelected
import no.nordicsemi.android.theme.view.BatteryLevelView
import no.nordicsemi.android.theme.view.ScreenSection
import no.nordicsemi.android.theme.view.SectionTitle

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
        SectionTitle(icon = Icons.Default.Settings, title = "Request items")

        Spacer(modifier = Modifier.height(16.dp))

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
        if (state.records.isEmpty()) {
            RecordsViewWithoutData()
        } else {
            RecordsViewWithData(state)
        }

    }
}

@Composable
private fun RecordsViewWithData(state: GLSData) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(resId = R.drawable.ic_records, title = "Records")

        Spacer(modifier = Modifier.height(16.dp))

        state.records.forEach {
            Text(text = String.format("Glucose concentration: %.2d", it.glucoseConcentration))
        }
    }
}

@Composable
private fun RecordsViewWithoutData() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionTitle(icon = Icons.Default.Search, title = "No items")

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(id = R.string.gls_no_records_info))
    }
}

@Preview
@Composable
private fun GLSContentView_NoData_Preview() {
    GLSContentView(GLSData()) { }
}

@Preview
@Composable
private fun GLSContentView_WithData_Preview() {
    GLSContentView(GLSData(records = listOf(
        GLSRecord(glucoseConcentration = 10f),
        GLSRecord(glucoseConcentration = 15f),
        GLSRecord(glucoseConcentration = 20f),
    ))) { }
}
