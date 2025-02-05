package no.nordicsemi.android.toolbox.profile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.data.NumberOfBytes
import no.nordicsemi.android.toolbox.profile.data.NumberOfSeconds
import no.nordicsemi.android.toolbox.profile.data.ThroughputServiceData
import no.nordicsemi.android.toolbox.profile.data.WritingStatus
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.displayThroughput
import no.nordicsemi.android.toolbox.profile.data.getThroughputInputTypes
import no.nordicsemi.android.toolbox.profile.data.throughputDataReceived
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.ThroughputEvent
import no.nordicsemi.android.ui.view.DropdownView
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
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ScreenSection {
            SectionTitle(
                icon = Icons.Default.SyncAlt,
                title = stringResource(id = R.string.throughput_service_name),
                menu = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(onClick = { showBottomSheet = true }) {
                            Text(stringResource(id = R.string.throughput_write))
                        }
                    }
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
        if (showBottomSheet) {
            ThroughputWriteBottomSheet(
                onDismiss = { showBottomSheet = false },
                onClickEvent
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThroughputWriteBottomSheet(
    onDismiss: () -> Unit,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var writeDataType by rememberSaveable { mutableStateOf("") }
    var isError: Boolean? by rememberSaveable { mutableStateOf(null) }
    var number by rememberSaveable { mutableIntStateOf(0) }
    var isNumberError: Boolean? by rememberSaveable { mutableStateOf(null) }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 16.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .width(50.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            DropdownView(
                items = getThroughputInputTypes(),
                label = stringResource(id = R.string.throughput_input_type),
                placeholder = stringResource(id = R.string.throughput_input_type_description),
                isError = isError == true,
                errorMessage = stringResource(id = R.string.throughput_input_type_error),
                onItemSelected = {
                    writeDataType = it
                    when (it) {
                        NumberOfBytes.getString() -> number = 102400
                        NumberOfSeconds.getString() -> number = 20
                    }
                }
            )
            when (writeDataType) {
                NumberOfBytes.getString() -> {
                    // Show bytes input
                    TextInputField(
                        input = number.toString(),
                        label = stringResource(id = R.string.throughput_bytes),
                        placeholder = stringResource(id = R.string.throughput_bytes_description),
                        errorState = isNumberError == true,
                        errorMessage = stringResource(id = R.string.throughput_bytes_error),
                        onUpdate = {
                            number = it.toIntOrNull() ?: 0

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
                        errorState = isNumberError == true,
                        errorMessage = stringResource(id = R.string.throughput_time_error),
                        onUpdate = {
                            number = it.toIntOrNull() ?: 0
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                }
            }
            // Confirm button.
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(),
                    onClick = {
                        if (writeDataType.isEmpty()) {
                            isError = true
                        } else if (number <= 0) {
                            isNumberError = true
                        } else {
                            onClickEvent(
                                ThroughputEvent.OnWriteData(
                                    when (writeDataType) {
                                        NumberOfBytes.getString() -> NumberOfBytes(number)
                                        NumberOfSeconds.getString() -> NumberOfSeconds(number)
                                        else -> throw IllegalArgumentException("Invalid throughput input type")
                                    }
                                )
                            )
                            onDismiss()
                        }
                    }
                ) {
                    Text(text = stringResource(id = R.string.throughput_start))
                }
            }
        }
    }
}

@Composable
fun ThroughputDataNotAvailable() {
    Text(stringResource(id = R.string.throughput_data_not_available))
}

@Preview
@Composable
private fun ThroughputScreenPreview() {
    ThroughputScreen(ThroughputServiceData()) {}
}
