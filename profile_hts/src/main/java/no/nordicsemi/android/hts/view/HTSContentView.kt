package no.nordicsemi.android.hts.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.hts.R
import no.nordicsemi.android.hts.data.HTSData
import no.nordicsemi.android.theme.view.BatteryLevelView
import no.nordicsemi.android.theme.view.KeyValueField
import no.nordicsemi.android.theme.view.ScreenSection
import no.nordicsemi.android.theme.view.SelectItemRadioGroup

@Composable
internal fun HTSContentView(state: HTSData, onEvent: (HTSScreenViewEvent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        ScreenSection {
            Box(modifier = Modifier.padding(16.dp)) {
                SelectItemRadioGroup(state.temperatureUnit, state.temperatureSettingsItems()) {
                    onEvent(OnTemperatureUnitSelected(it.unit))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ScreenSection {
            KeyValueField(
                stringResource(id = R.string.hts_temperature),
                state.displayTemperature()
            )
        }

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

@Preview
@Composable
private fun Preview() {
    HTSContentView(state = HTSData()) { }
}
