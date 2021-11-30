package no.nordicsemi.android.prx.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.prx.R
import no.nordicsemi.android.prx.data.PRXData
import no.nordicsemi.android.theme.view.BatteryLevelView
import no.nordicsemi.android.theme.view.KeyValueField
import no.nordicsemi.android.theme.view.ScreenSection

@Composable
internal fun ContentView(state: PRXData, onEvent: (PRXScreenViewEvent) -> Unit) {
    ScreenSection {
        Column {
            KeyValueField(
                stringResource(id = R.string.prx_is_remote_alarm),
                state.isRemoteAlarm.toString()
            )
            Spacer(modifier = Modifier.height(4.dp))
            KeyValueField(
                stringResource(id = R.string.prx_local_alarm_level),
                state.displayLocalAlarm()
            )
        }
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
