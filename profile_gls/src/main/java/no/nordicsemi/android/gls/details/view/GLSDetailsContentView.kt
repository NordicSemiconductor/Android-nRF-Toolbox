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

package no.nordicsemi.android.gls.details.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.gls.R
import no.nordicsemi.android.gls.data.GLSRecord
import no.nordicsemi.android.gls.main.view.toDisplayString

@Composable
internal fun GLSDetailsContentView(record: GLSRecord) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedCard {
                Field(
                    stringResource(id = R.string.gls_details_sequence_number),
                    record.sequenceNumber.toString()
                )

                record.time?.let {
                    Field(
                        stringResource(id = R.string.gls_details_date_and_time),
                        stringResource(R.string.gls_timestamp, it)
                    )
                }

                Divider(
                    color = MaterialTheme.colorScheme.secondary,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                record.type?.let {
                    Field(stringResource(id = R.string.gls_details_type), it.toDisplayString())
                    Spacer(modifier = Modifier.size(4.dp))
                }

                record.sampleLocation?.let {
                    Field(stringResource(id = R.string.gls_details_location), it.toDisplayString())
                    Spacer(modifier = Modifier.size(4.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = stringResource(id = R.string.gls_details_glucose_condensation_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = stringResource(
                            id = R.string.gls_details_glucose_condensation_field,
                            record.glucoseConcentration,
                            record.unit.toDisplayString()
                        ),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                record.status?.let {
                    Divider(
                        color = MaterialTheme.colorScheme.secondary,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    BooleanField(
                        stringResource(id = R.string.gls_details_battery_low),
                        it.deviceBatteryLow
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    BooleanField(
                        stringResource(id = R.string.gls_details_sensor_malfunction),
                        it.sensorMalfunction
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    BooleanField(
                        stringResource(id = R.string.gls_details_insufficient_sample),
                        it.sampleSizeInsufficient
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    BooleanField(
                        stringResource(id = R.string.gls_details_strip_insertion_error),
                        it.stripInsertionError
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    BooleanField(
                        stringResource(id = R.string.gls_details_strip_type_incorrect),
                        it.stripTypeIncorrect
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    BooleanField(
                        stringResource(id = R.string.gls_details_sensor_result_too_high),
                        it.sensorResultHigherThenDeviceCanProcess
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    BooleanField(
                        stringResource(id = R.string.gls_details_sensor_result_too_low),
                        it.sensorResultLowerThenDeviceCanProcess
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    BooleanField(
                        stringResource(id = R.string.gls_details_temperature_too_high),
                        it.sensorTemperatureTooHigh
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    BooleanField(
                        stringResource(id = R.string.gls_details_temperature_too_low),
                        it.sensorTemperatureTooLow
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    BooleanField(
                        stringResource(id = R.string.gls_details_strip_pulled_too_soon),
                        it.sensorReadInterrupted
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    BooleanField(
                        stringResource(id = R.string.gls_details_general_device_fault),
                        it.generalDeviceFault
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    BooleanField(stringResource(id = R.string.gls_details_time_fault), it.timeFault)
                    Spacer(modifier = Modifier.size(4.dp))
                }

                record.context?.let {
                    Divider(
                        color = MaterialTheme.colorScheme.secondary,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    Field(
                        stringResource(id = R.string.gls_context_title),
                        stringResource(id = R.string.gls_available)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    it.carbohydrate?.let {
                        Field(
                            stringResource(id = R.string.gls_context_carbohydrate),
                            it.toDisplayString()
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                    it.meal?.let {
                        Field(stringResource(id = R.string.gls_context_meal), it.toDisplayString())
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                    it.tester?.let {
                        Field(
                            stringResource(id = R.string.gls_context_tester),
                            it.toDisplayString()
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                    it.health?.let {
                        Field(
                            stringResource(id = R.string.gls_context_health),
                            it.toDisplayString()
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                    Field(
                        stringResource(id = R.string.gls_context_exercise_title),
                        stringResource(
                            id = R.string.gls_context_exercise_field,
                            it.exerciseDuration,
                            it.exerciseIntensity
                        )
                    )
                    Spacer(modifier = Modifier.size(4.dp))

                    val medicationField = String.format(
                        stringResource(id = R.string.gls_context_medication_field),
                        it.medicationQuantity,
                        it.medicationUnit.toDisplayString(),
                        it.medication?.toDisplayString()
                    )
                    Field(
                        stringResource(id = R.string.gls_context_medication_title),
                        medicationField
                    )

                    Spacer(modifier = Modifier.size(4.dp))
                    Field(
                        stringResource(id = R.string.gls_context_hba1c_title),
                        stringResource(id = R.string.gls_context_hba1c_field, it.HbA1c)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                } ?: Field(
                    stringResource(id = R.string.gls_context_title),
                    stringResource(id = R.string.gls_unavailable)
                )
            }
        }
    }
}
