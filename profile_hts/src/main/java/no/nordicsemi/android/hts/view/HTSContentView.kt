package no.nordicsemi.android.hts.view

import androidx.compose.foundation.layout.*
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
import no.nordicsemi.android.material.you.RadioButtonGroup
import no.nordicsemi.android.theme.view.BatteryLevelView
import no.nordicsemi.android.theme.view.KeyValueField
import no.nordicsemi.android.theme.view.ScreenSection
import no.nordicsemi.android.theme.view.SectionTitle

@Composable
internal fun HTSContentView(state: HTSData, temperatureUnit: TemperatureUnit, onEvent: (HTSScreenViewEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenSection {
            SectionTitle(resId = R.drawable.ic_thermometer, title = "Settings")

            Spacer(modifier = Modifier.height(16.dp))

            RadioButtonGroup(viewEntity = temperatureUnit.temperatureSettingsItems()) {
                onEvent(OnTemperatureUnitSelected(it.label.toTemperatureUnit()))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ScreenSection {
            SectionTitle(resId = R.drawable.ic_records, title = stringResource(id = R.string.hts_records_section))

            Spacer(modifier = Modifier.height(16.dp))

            KeyValueField(
                stringResource(id = R.string.hts_temperature),
                displayTemperature(state.temperatureValue, temperatureUnit)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        state.batteryLevel?.let {
            BatteryLevelView(it)

            Spacer(modifier = Modifier.height(16.dp))
        }

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
    HTSContentView(state = HTSData(), TemperatureUnit.CELSIUS) { }
}
