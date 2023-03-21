/*
 * Copyright (c) 2022, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.prx.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import no.nordicsemi.android.prx.data.PRXServiceData
import no.nordicsemi.android.ui.view.BatteryLevelView
import no.nordicsemi.android.ui.view.KeyValueField
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun ContentView(state: PRXServiceData, onEvent: (PRXScreenViewEvent) -> Unit) {
    Column(
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
private fun SettingsSection(state: PRXServiceData, onEvent: (PRXScreenViewEvent) -> Unit) {
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
private fun RecordsSection(state: PRXServiceData) {
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
