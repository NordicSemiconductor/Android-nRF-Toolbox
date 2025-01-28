package no.nordicsemi.android.toolbox.profile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.libs.core.data.ThroughputServiceData
import no.nordicsemi.android.toolbox.libs.core.data.WriteDataType
import no.nordicsemi.android.toolbox.profile.data.throughputDataReceived
import no.nordicsemi.android.toolbox.profile.view.ThroughputSettingsMenu.Companion.onClick
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.ThroughputEvent
import no.nordicsemi.android.ui.view.DropdownView
import no.nordicsemi.android.ui.view.KeyValueColumn
import no.nordicsemi.android.ui.view.KeyValueColumnReverse
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionRow
import no.nordicsemi.android.ui.view.SectionTitle
import no.nordicsemi.android.ui.view.TextInputField
import timber.log.Timber

internal enum class ThroughputSettingsMenu {
    RequestMtu,
    ResetMetrics;

    override fun toString(): String {
        return when (this) {
            RequestMtu -> "Highest MTU"
            ResetMetrics -> "Reset metrics"
        }
    }

    companion object {
        fun ThroughputSettingsMenu.onClick(onClickEvent: (DeviceConnectionViewEvent) -> Unit) {
            when (this) {
                RequestMtu -> onClickEvent(ThroughputEvent.RequestMtuSize)
                ResetMetrics -> onClickEvent(ThroughputEvent.OnResetClick)
            }
        }
    }
}

@Composable
internal fun ThroughputScreen(
    serviceData: ThroughputServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    var openSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ScreenSection {
            SectionTitle(
                icon = Icons.Default.SyncAlt,
                title = "Throughput service",
                menu = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(onClick = { showBottomSheet = true }) {
                            Text("Write")
                        }
                        ThroughputSettings(
                            isOpenSettingDialog = openSettingsDialog,
                            onExpand = { openSettingsDialog = true },
                            onDismiss = { openSettingsDialog = false }) { onClickEvent(it) }
                    }
                }
            )
            serviceData.throughputData?.let {
                SectionRow {
                    KeyValueColumn(
                        "Total bytes received",
                        it.throughputDataReceived()
                    )
                    KeyValueColumnReverse(
                        "Gatt writes",
                        it.gattWritesReceived.toString()
                    )
                }
                SectionRow {
                    KeyValueColumn(
                        "Throughput",
                        "${it.throughputBitsPerSecond} bps"
                    )
                }
            } ?: run {
                ThroughputDataNotAvailable()
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
    var writeDataType by rememberSaveable { mutableStateOf(WriteDataType.TEXT) }
    var data by rememberSaveable { mutableStateOf("") }
    var isError by rememberSaveable { mutableStateOf(false) }

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
                items = WriteDataType.entries.map { it },
                label = "Write type",
                placeholder = "Select write type",
                defaultSelectedItem = WriteDataType.TEXT,
                onItemSelected = {
                    writeDataType = it
                }
            )
            TextInputField(
                input = data,
                label = "Input",
                placeholder = "Enter input data here.",
                errorMessage = "Input data can't be empty.",
                errorState = data.trim().isEmpty() && isError,
                onUpdate = {
                    data = it
                    if (data.trim().isEmpty()) {
                        isError = true
                    }
                }
            )
            // Confirm button.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable {
                        if (isError && data.trim().isNotEmpty()) {
                            onClickEvent(ThroughputEvent.OnWriteData(writeDataType, data))
                            onDismiss()
                            Timber.tag("AAA").d("Confirm button clicked!")
                        } else isError = true
                    },
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(),
                    onClick = {
                        if (isError && data.trim().isNotEmpty()) {
                            onClickEvent(ThroughputEvent.OnWriteData(writeDataType, data))
                            onDismiss()
                            Timber.tag("AAA").d("Confirm button clicked!")
                        } else isError = true
                    }
                ) {
                    Text(text = "Confirm")
                }
            }
        }
    }

}

@Composable
fun ThroughputDataNotAvailable() {
    Text(
        "No throughput metrics to show. Please click settings to reset the metrics or request highest MTU. Click on write button to write data."
    )
}

@Preview
@Composable
private fun ThroughputScreenPreview() {
    ThroughputScreen(ThroughputServiceData()) {}
}

@Composable
fun ThroughputSettings(
    isOpenSettingDialog: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    Icon(
        imageVector = Icons.Default.Settings,
        contentDescription = null,
        modifier = Modifier
            .clickable { onExpand() }
            .clip(CircleShape)
            .padding(4.dp)
    )

    if (isOpenSettingDialog)
        ThroughSettingsDialog(
            onDismiss = onDismiss,
        ) {
            onClickEvent(it)
            onDismiss()
        }
}

@Composable
private fun ThroughSettingsDialog(
    onDismiss: () -> Unit,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit,
) {
    val workingModeEntries = ThroughputSettingsMenu.entries.map { it }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Request feature") },
        text = {
            LazyColumn {
                items(workingModeEntries.size) { index ->
                    val entry = workingModeEntries[index]
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                entry.onClick { onClickEvent(it) }
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.toString(),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ThroughputSettingsDialogPreview() {
    ThroughSettingsDialog({}) {}
}
