package no.nordicsemi.android.toolbox.profile.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import no.nordicsemi.android.toolbox.profile.view.ThroughputSettingsMenu.Companion.onClick
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.ThroughputEvent
import no.nordicsemi.android.ui.view.KeyValueColumn
import no.nordicsemi.android.ui.view.KeyValueColumnReverse
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionRow
import no.nordicsemi.android.ui.view.SectionTitle

internal enum class ThroughputSettingsMenu {
    RequestMtu,
    ResetMetrics;

    override fun toString(): String {
        return when (this) {
            RequestMtu -> "Highest mtu value"
            ResetMetrics -> "Reset throughput metrics"
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

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ScreenSection {
            SectionTitle(
                icon = Icons.Default.SyncAlt,
                title = "Throughput service",
                menu = {
                    ThroughputSettings(
                        isOpenSettingDialog = openSettingsDialog,
                        onExpand = { openSettingsDialog = true },
                        onDismiss = { openSettingsDialog = false }) { onClickEvent(it) }
                }
            )

            Button(onClick = { onClickEvent(ThroughputEvent.OnWriteData) }) {
                Text("Write data")
            }

            serviceData.throughputData?.let {
                SectionRow {
                    KeyValueColumn(
                        "Total bytes received",
                        "${it.totalBytesReceived} bytes"
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
            }
        }
    }

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
    Column {
        OutlinedButton(onClick = { onExpand() }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Request")
                Icon(Icons.Default.ArrowDropDown, contentDescription = "")
            }
        }
        if (isOpenSettingDialog)
            ThroughSettingsDialog(
                onDismiss = onDismiss,
            ) {
                onClickEvent(it)
                onDismiss()
            }
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
                            .padding(8.dp),
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
