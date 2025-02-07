package no.nordicsemi.android.toolbox.profile.view.throughput

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.NumberOfBytes
import no.nordicsemi.android.toolbox.profile.data.NumberOfSeconds
import no.nordicsemi.android.toolbox.profile.data.ThroughputServiceData
import no.nordicsemi.android.toolbox.profile.data.WritingStatus
import no.nordicsemi.android.toolbox.profile.data.displayThroughput
import no.nordicsemi.android.toolbox.profile.data.throughputDataReceived
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.ThroughputEvent
import no.nordicsemi.android.ui.view.KeyValueColumn
import no.nordicsemi.android.ui.view.KeyValueColumnReverse
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionRow
import no.nordicsemi.android.ui.view.SectionTitle
import no.nordicsemi.android.ui.view.TextInputField

@Composable
internal fun ThroughputScreen(
    serviceData: ThroughputServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        ScreenSection {
            SectionTitle(
                icon = Icons.Default.SyncAlt,
                title = stringResource(id = R.string.throughput_service_name),
                menu = {
                    var expanded by rememberSaveable { mutableStateOf(false) }
                    var number by rememberSaveable { mutableIntStateOf(0) }
                    var writeDataType by rememberSaveable { mutableStateOf("") }

                    WriteDropdown(
                        expanded = expanded,
                        writeDataType = writeDataType,
                        number = number,
                        onDropdownMenuSelected = { writeDataType = it },
                        onNumberUpdate = { number = it },
                        onDismiss = {
                            expanded = false
                            writeDataType = ""
                            number = 0
                        },
                        onExpand = { expanded = true },
                        onClickEvent = onClickEvent
                    )
                }
            )
            when (serviceData.writingStatus) {
                WritingStatus.IDEAL -> {
                    ThroughputDataNotAvailable()
                }

                WritingStatus.IN_PROGRESS -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(stringResource(id = R.string.write_inprogress))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                WritingStatus.COMPLETED -> {
                    // Show throughput data.
                    serviceData.throughputData?.let {
                        SectionRow {
                            KeyValueColumn(
                                stringResource(id = R.string.total_bytes_received),
                                it.throughputDataReceived()
                            )
                            KeyValueColumnReverse(
                                stringResource(id = R.string.gatt_write_number),
                                it.gattWritesReceived.toString()
                            )
                        }
                        SectionRow {
                            KeyValueColumn(
                                stringResource(id = R.string.measured_throughput),
                                it.displayThroughput()
                            )
                            // Show mtu size
                            serviceData.maxWriteValueLength?.let {
                                KeyValueColumnReverse(
                                    stringResource(id = R.string.max_write_value),
                                    "$it"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WriteDropdown(
    expanded: Boolean,
    number: Int,
    writeDataType: String,
    onDismiss: () -> Unit,
    onExpand: () -> Unit,
    onDropdownMenuSelected: (String) -> Unit,
    onNumberUpdate: (Int) -> Unit,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    Box(modifier = Modifier.padding(8.dp)) {
        Button(onClick = { onExpand() }) {
            Text(stringResource(id = R.string.throughput_write))
        }
        // Animated dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onDismiss() },
            modifier = Modifier.padding(8.dp)
        ) {
            when (writeDataType) {
                NumberOfBytes.getString() -> {
                    // Show bytes input
                    TextInputField(
                        input = number.toString(),
                        label = stringResource(id = R.string.throughput_bytes),
                        placeholder = stringResource(id = R.string.throughput_bytes_description),
                        errorState = number < 0,
                        errorMessage = stringResource(id = R.string.throughput_bytes_error),
                        onUpdate = {
                            onNumberUpdate(it.toIntOrNull() ?: 0)

                        },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                }

                NumberOfSeconds.getString() -> {
                    // Show time input
                    TextInputField(
                        input = number.toString(),
                        label = stringResource(id = R.string.throughput_time),
                        placeholder = stringResource(id = R.string.throughput_time_description),
                        errorState = number < 0,
                        errorMessage = stringResource(id = R.string.throughput_time_error),
                        onUpdate = {
                            onNumberUpdate(it.toIntOrNull() ?: 0)
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                }

                else -> {
                    // Show throughput input type
                    DropdownMenuItem(
                        text = { Text(NumberOfBytes.getString()) },
                        onClick = {
                            onDropdownMenuSelected(NumberOfBytes.getString())
                            onNumberUpdate(100)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(NumberOfSeconds.getString()) },
                        onClick = {
                            onDropdownMenuSelected(NumberOfSeconds.getString())
                            onNumberUpdate(20)
                        }
                    )
                }
            }
            // Run button.
            if (writeDataType.isNotEmpty() && number > 0) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(),
                        onClick = {
                            onClickEvent(
                                ThroughputEvent.OnWriteData(
                                    when (writeDataType) {
                                        NumberOfBytes.getString() -> NumberOfBytes(number * 1024)
                                        NumberOfSeconds.getString() -> NumberOfSeconds(number)
                                        else -> throw IllegalArgumentException("Invalid throughput input type")
                                    }
                                )
                            )
                            onDismiss()
                        }
                    ) { Text(text = stringResource(id = R.string.throughput_start)) }
                }
            }
        }
    }
}

@Composable
private fun ThroughputDataNotAvailable() {
    Text(stringResource(id = R.string.throughput_data_not_available))
}

@Preview
@Composable
private fun ThroughputScreenPreview() {
    ThroughputScreen(ThroughputServiceData()) {}
}
