package no.nordicsemi.android.toolbox.profile.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.libs.core.data.RSCSServiceData
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.displayActivity
import no.nordicsemi.android.toolbox.profile.data.displayNumberOfSteps
import no.nordicsemi.android.toolbox.profile.data.displayPace
import no.nordicsemi.android.toolbox.profile.data.displaySpeed
import no.nordicsemi.android.toolbox.profile.data.displayStrideLength
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.RSCSViewEvent
import no.nordicsemi.android.ui.view.KeyValueColumn
import no.nordicsemi.android.ui.view.KeyValueColumnReverse
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionRow
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun RSCSScreen(
    serviceData: RSCSServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ScreenSection {
            SectionTitle(
                resId = R.drawable.ic_rscs,
                title = if (serviceData.data.running) "Running" else "Walking",
                menu = { RSCSSettingsDropdown(serviceData, onClickEvent) }
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionRow {
                    KeyValueColumn(
                        stringResource(id = R.string.rscs_cadence),
                        serviceData.displayPace()
                    )
                    KeyValueColumnReverse(
                        value = stringResource(id = R.string.rscs_activity),
                        key = if (serviceData.data.running)
                            "\uD83C\uDFC3 ${serviceData.displayActivity()}" else
                            "\uD83D\uDEB6 ${serviceData.displayActivity()}"
                    )
                }
                SectionRow {
                    KeyValueColumn("Speed", "${serviceData.displaySpeed()}")
                    serviceData.displayNumberOfSteps()?.let {
                        KeyValueColumnReverse(
                            stringResource(id = R.string.rscs_number_of_steps),
                            it
                        )
                    } ?: serviceData.displayStrideLength()?.let {
                        KeyValueColumnReverse(
                            stringResource(id = R.string.stride_length), it
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RSCSScreenPreview() {
    RSCSScreen(RSCSServiceData()) {}
}

@Composable
private fun RSCSSettingsDropdown(
    state: RSCSServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    var openSettingsDialog by rememberSaveable { mutableStateOf(false) }

    Column {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "display settings",
            modifier = Modifier
                .clip(CircleShape)
                .clickable { openSettingsDialog = true }
                .padding(8.dp)
        )

        if (openSettingsDialog) {
            RSCSSettingsDialog(state, { openSettingsDialog = false }, onClickEvent)
        }
    }
}

@Composable
fun RSCSSettingsDialog(
    state: RSCSServiceData,
    onDismiss: () -> Unit,
    onSpeedUnitSelected: (DeviceConnectionViewEvent) -> Unit
) {
    val listState = rememberLazyListState()
    val speedUnitEntries = no.nordicsemi.android.lib.profile.rscs.RSCSSettingsUnit.entries.map { it }
    val selectedIndex = speedUnitEntries.indexOf(state.unit)

    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0) {
            listState.scrollToItem(selectedIndex)
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(R.string.rscs_settings_unit_title)) },
        text = {
            LazyColumn(
                state = listState
            ) {
                items(speedUnitEntries.size) { index ->
                    val entry = speedUnitEntries[index]
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onSpeedUnitSelected(RSCSViewEvent.OnSelectedSpeedUnitSelected(entry))
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.toString(),
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                                .clickable {
                                    onSpeedUnitSelected(
                                        RSCSViewEvent.OnSelectedSpeedUnitSelected(entry)
                                    )
                                    onDismiss()
                                },
                            color = if (state.unit == entry)
                                MaterialTheme.colorScheme.primary else
                                MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}

