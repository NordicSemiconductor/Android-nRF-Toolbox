package no.nordicsemi.android.toolbox.profile.view.hts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import no.nordicsemi.android.lib.profile.hts.HTSData
import no.nordicsemi.android.lib.profile.hts.HTSMeasurementType
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.HTSServiceData
import no.nordicsemi.android.toolbox.profile.data.uiMapper.TemperatureUnit
import no.nordicsemi.android.toolbox.profile.viewmodel.HTSEvent
import no.nordicsemi.android.ui.view.KeyValueColumn
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionRow
import no.nordicsemi.android.ui.view.SectionTitle
import java.util.Calendar

@Composable
internal fun HTSScreen(
    htsServiceData: HTSServiceData,
    onClickEvent: (HTSEvent) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ScreenSection {
            SectionTitle(
                resId = R.drawable.ic_hts,
                title = stringResource(id = R.string.hts_temperature),
                menu = {
                    TemperatureUnitSettings(
                        state = htsServiceData,
                        onClickEvent = { onClickEvent(it) },
                    )
                }
            )
            SectionRow {
                KeyValueColumn(
                    value = "Temperature",
                    key = htsServiceData.data?.temperature?.let {
                        htsServiceData.temperatureUnit.displayTemperature(it)
                    } ?: run { "Reading temperature..." },
                    keyStyle = MaterialTheme.typography.titleMedium

                )
            }
            if (htsServiceData.data?.type != null) {
                SectionRow {
                    KeyValueColumn(
                        value = "Temperature measurement location",
                        key = htsServiceData.data!!.type?.let {
                            HTSMeasurementType.fromValue(it).toString()
                        } ?: "Unknown",
                        keyStyle = MaterialTheme.typography.titleMedium
                    )
                }
            }
            htsServiceData.data?.timestamp?.let {
                SectionRow {
                    KeyValueColumn(
                        value = "Measurement time",
                        key = it.toFormattedString(),
                        keyStyle = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HTSScreenPreview() {
    HTSScreen(
        HTSServiceData(
            data = HTSData(
                temperature = 36.5f,
                type = HTSMeasurementType.TOE.value,
                timestamp = Calendar.getInstance()
            ),
            temperatureUnit = TemperatureUnit.CELSIUS
        )
    ) { }
}

@Composable
private fun TemperatureUnitSettings(
    state: HTSServiceData,
    onClickEvent: (HTSEvent) -> Unit
) {
    var openSettingsDialog by rememberSaveable { mutableStateOf(false) }

    Column {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = stringResource(id = R.string.hts_temperature_unit_des),
            modifier = Modifier
                .clip(CircleShape)
                .clickable { openSettingsDialog = true }
        )
        if (openSettingsDialog) {
            TemperatureUnitSettingsDialog(
                state, { openSettingsDialog = false }
            ) { onClickEvent(it) }
        }
    }
}

@Composable
private fun TemperatureUnitSettingsDialog(
    state: HTSServiceData,
    onDismiss: () -> Unit,
    onClickEvent: (HTSEvent) -> Unit,
) {
    val listState = rememberLazyListState()
    val entries = TemperatureUnit.entries.map { it }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.hts_temperature_unit),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                LazyColumn(
                    state = listState
                ) {
                    items(entries.size) { index ->
                        val entry = entries[index]
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    onClickEvent(HTSEvent.OnTemperatureUnitSelected(entry))
                                    onDismiss()
                                }
                                .padding(8.dp),
                        ) {
                            Text(
                                text = entry.toString(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onClickEvent(HTSEvent.OnTemperatureUnitSelected(entry))
                                        onDismiss()
                                    }
                                    .padding(8.dp),
                                style = MaterialTheme.typography.titleLarge,
                                color = if (state.temperatureUnit == entry)
                                    MaterialTheme.colorScheme.primary else
                                    MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TemperatureUnitSettingsDialogPreview() {
    TemperatureUnitSettingsDialog(
        state = HTSServiceData(),
        onDismiss = {},
        onClickEvent = {}
    )
}