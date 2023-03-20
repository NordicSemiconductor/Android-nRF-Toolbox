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

package no.nordicsemi.android.hts.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.theme.view.RadioButtonGroup
import no.nordicsemi.android.hts.R
import no.nordicsemi.android.hts.data.HTSServiceData
import no.nordicsemi.android.ui.view.BatteryLevelView
import no.nordicsemi.android.ui.view.KeyValueField
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun HTSContentView(state: HTSServiceData, onEvent: (HTSScreenViewEvent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenSection {
            SectionTitle(resId = R.drawable.ic_thermometer, title = "Settings")

            Spacer(modifier = Modifier.height(16.dp))

            RadioButtonGroup(viewEntity = state.temperatureUnit.temperatureSettingsItems()) {
                onEvent(OnTemperatureUnitSelected(it.label.toTemperatureUnit()))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ScreenSection {
            SectionTitle(resId = R.drawable.ic_records, title = stringResource(id = R.string.hts_records_section))

            Spacer(modifier = Modifier.height(16.dp))

            KeyValueField(
                stringResource(id = R.string.hts_temperature),
                displayTemperature(state.data.temperature, state.temperatureUnit)
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
    HTSContentView(state = HTSServiceData()) { }
}
