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

package no.nordicsemi.android.csc.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import no.nordicsemi.android.common.theme.view.RadioButtonGroup
import no.nordicsemi.android.csc.R
import no.nordicsemi.android.csc.data.CSCServiceData
import no.nordicsemi.android.csc.data.SpeedUnit
import no.nordicsemi.android.kotlin.ble.profile.csc.data.CSCData
import no.nordicsemi.android.kotlin.ble.profile.csc.data.WheelSize
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle
import no.nordicsemi.android.ui.view.dialog.FlowCanceled
import no.nordicsemi.android.ui.view.dialog.ItemSelectedResult

@Composable
internal fun CSCContentView(state: CSCServiceData, onEvent: (CSCViewEvent) -> Unit) {
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
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SettingsSection(state.data, state.speedUnit, onEvent) { showDialog.value = true }

        Spacer(modifier = Modifier.height(16.dp))

        SensorsReadingView(state = state, state.speedUnit)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onEvent(OnDisconnectButtonClick) }
        ) {
            Text(text = stringResource(id = R.string.disconnect))
        }
    }
}

@Composable
private fun SettingsSection(
    state: CSCData,
    speedUnit: SpeedUnit,
    onEvent: (CSCViewEvent) -> Unit,
    onWheelButtonClick: () -> Unit,
) {
    ScreenSection {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionTitle(icon = Icons.Default.Settings,
                title = stringResource(R.string.csc_settings))

            Spacer(modifier = Modifier.height(16.dp))

            WheelSizeView(state, onWheelButtonClick)

            Spacer(modifier = Modifier.height(16.dp))

            RadioButtonGroup(viewEntity = speedUnit.temperatureSettingsItems()) {
                onEvent(OnSelectedSpeedUnitSelected(it.label.toSpeedUnit()))
            }
        }
    }
}

@Preview
@Composable
private fun ConnectedPreview() {
    CSCContentView(CSCServiceData()) { }
}
