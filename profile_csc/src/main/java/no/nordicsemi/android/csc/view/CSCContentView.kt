package no.nordicsemi.android.csc.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.csc.R
import no.nordicsemi.android.csc.data.CSCData
import no.nordicsemi.android.csc.data.WheelSize
import no.nordicsemi.android.material.you.RadioButtonGroup
import no.nordicsemi.android.theme.view.ScreenSection
import no.nordicsemi.android.theme.view.SectionTitle
import no.nordicsemi.android.theme.view.dialog.FlowCanceled
import no.nordicsemi.android.theme.view.dialog.ItemSelectedResult
import no.nordicsemi.android.utils.exhaustive

@Composable
internal fun CSCContentView(state: CSCData, onEvent: (CSCViewEvent) -> Unit) {
    val showDialog = rememberSaveable { mutableStateOf(false) }

    if (showDialog.value) {
        val wheelEntries = stringArrayResource(R.array.wheel_entries)
        val wheelValues = stringArrayResource(R.array.wheel_values)

        SelectWheelSizeDialog {
            when (it) {
                FlowCanceled -> showDialog.value = false
                is ItemSelectedResult -> {
                    onEvent(OnWheelSizeSelected(WheelSize(wheelValues[it.index].toInt(), wheelEntries[it.index])))
                    showDialog.value = false
                }
            }.exhaustive
        }
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            SettingsSection(state, onEvent) { showDialog.value = true }

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
}

@Composable
private fun SettingsSection(state: CSCData, onEvent: (CSCViewEvent) -> Unit, onWheelButtonClick: () -> Unit) {
    ScreenSection {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionTitle(icon = Icons.Default.Settings, title = stringResource(R.string.csc_settings))

            Spacer(modifier = Modifier.height(16.dp))

            WheelSizeView(state, onWheelButtonClick)

            Spacer(modifier = Modifier.height(16.dp))

            RadioButtonGroup(viewEntity = state.temperatureSettingsItems()) {
                onEvent(OnSelectedSpeedUnitSelected(state.getSpeedUnit(it.label)))
            }
        }
    }
}

@Preview
@Composable
private fun ConnectedPreview() {
    CSCContentView(CSCData()) { }
}
