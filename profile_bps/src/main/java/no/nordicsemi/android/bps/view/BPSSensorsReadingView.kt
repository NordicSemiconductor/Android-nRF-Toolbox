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

package no.nordicsemi.android.bps.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.bps.R
import no.nordicsemi.android.bps.data.BPSServiceData
import no.nordicsemi.android.kotlin.ble.profile.bps.data.BloodPressureMeasurementData
import no.nordicsemi.android.kotlin.ble.profile.bps.data.IntermediateCuffPressureData
import no.nordicsemi.android.ui.view.BatteryLevelView
import no.nordicsemi.android.ui.view.KeyValueField
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun BPSSensorsReadingView(state: BPSServiceData) {
    ScreenSection {
        Column {
            SectionTitle(resId = R.drawable.ic_records, title = stringResource(id = R.string.bps_records))

            state.bloodPressureMeasurement?.let {
                Spacer(modifier = Modifier.height(16.dp))
                BloodPressureView(it)
            }

            state.intermediateCuffPressure?.displayHeartRate()?.let {
                Spacer(modifier = Modifier.height(4.dp))
                KeyValueField(stringResource(id = R.string.bps_pulse), it)
            }

            if (state.intermediateCuffPressure == null && state.bloodPressureMeasurement == null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(id = R.string.no_data_info),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    state.batteryLevel?.let {
        BatteryLevelView(it)
    }
}

@Composable
private fun BloodPressureView(state: BloodPressureMeasurementData) {
    KeyValueField(stringResource(id = R.string.bps_systolic), state.displaySystolic())
    Spacer(modifier = Modifier.height(4.dp))
    KeyValueField(stringResource(id = R.string.bps_diastolic), state.displayDiastolic())
    Spacer(modifier = Modifier.height(4.dp))
    KeyValueField(stringResource(id = R.string.bps_mean), state.displayMeanArterialPressure())
}

@Composable
fun BloodPressureMeasurementData.displaySystolic(): String {
    return stringResource(id = R.string.bps_blood_pressure, systolic)
}

@Composable
fun BloodPressureMeasurementData.displayDiastolic(): String {
    return stringResource(id = R.string.bps_blood_pressure, diastolic)
}

@Composable
fun BloodPressureMeasurementData.displayMeanArterialPressure(): String {
    return stringResource(id = R.string.bps_blood_pressure, meanArterialPressure)
}

@Composable
fun IntermediateCuffPressureData.displayHeartRate(): String? {
    return pulseRate?.toString()
}

@Preview
@Composable
private fun Preview() {
    BPSSensorsReadingView(BPSServiceData())
}
