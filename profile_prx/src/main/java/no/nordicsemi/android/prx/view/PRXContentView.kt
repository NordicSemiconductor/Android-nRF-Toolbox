package no.nordicsemi.android.prx.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.prx.R
import no.nordicsemi.android.prx.data.PRXData
import no.nordicsemi.android.theme.view.BatteryLevelView
import no.nordicsemi.android.theme.view.KeyValueField
import no.nordicsemi.android.theme.view.ScreenSection
import no.nordicsemi.android.theme.view.SectionTitle

@Composable
internal fun ContentView(state: PRXData, onEvent: (PRXScreenViewEvent) -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SettingsSection(state, onEvent)

        Spacer(modifier = Modifier.height(16.dp))

        RecordsSection(state)

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

@Composable
private fun SettingsSection(state: PRXData, onEvent: (PRXScreenViewEvent) -> Unit) {
    ScreenSection {
        SectionTitle(icon = Icons.Default.Settings, title = stringResource(R.string.prx_settings))

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isRemoteAlarm) {
            TurnAlarmOffButton(onEvent)
        } else {
            TurnAlarmOnButton(onEvent)
        }
    }
}

@Composable
private fun TurnAlarmOnButton(onEvent: (PRXScreenViewEvent) -> Unit) {
    Button(
        onClick = { onEvent(TurnOnAlert) }
    ) {
        Text(text = stringResource(id = R.string.prx_find_me))
    }
}

@Composable
private fun TurnAlarmOffButton(onEvent: (PRXScreenViewEvent) -> Unit) {
    Button(
        onClick = { onEvent(TurnOffAlert) }
    ) {
        Text(text = stringResource(id = R.string.prx_silent_me))
    }
}

@Composable
private fun RecordsSection(state: PRXData) {
    ScreenSection {
        SectionTitle(resId = R.drawable.ic_records, title = stringResource(id = R.string.prx_records))

        Spacer(modifier = Modifier.height(16.dp))

        Column {
            KeyValueField(
                stringResource(id = R.string.prx_is_remote_alarm),
                state.isRemoteAlarm.toDisplayString()
            )
            Spacer(modifier = Modifier.height(4.dp))
            KeyValueField(
                stringResource(id = R.string.prx_local_alarm_level),
                state.localAlarmLevel.toDisplayString().uppercase()
            )
        }
    }
}
