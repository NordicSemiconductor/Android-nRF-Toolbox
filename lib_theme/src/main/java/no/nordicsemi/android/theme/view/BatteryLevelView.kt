package no.nordicsemi.android.theme.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.theme.R

@Composable
fun BatteryLevelView(batteryLevel: Int) {
    ScreenSection {
        KeyValueField(
            stringResource(id = R.string.field_battery),
            "$batteryLevel%"
        )
    }
}