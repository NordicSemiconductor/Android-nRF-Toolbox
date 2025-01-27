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
import no.nordicsemi.android.toolbox.libs.core.data.HTSServiceData
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.displayTemperature
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.HTSViewEvent
import no.nordicsemi.android.ui.view.KeyValueColumn
import no.nordicsemi.android.ui.view.KeyValueColumnReverse
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionRow
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun HTSScreen(
    htsServiceData: HTSServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ScreenSection {
            SectionTitle(
                resId = R.drawable.ic_thermometer,
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
                    stringResource(id = R.string.hts_temperature),
                    htsServiceData.data?.temperature?.let {
                        displayTemperature(it, htsServiceData.temperatureUnit)
                    } ?: run { "__" }
                )
                KeyValueColumnReverse(
                    stringResource(id = R.string.hts_temperature_unit_title),
                    "${htsServiceData.temperatureUnit}"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HTSScreenPreview() {
    HTSScreen(HTSServiceData()) { }
}

@Composable
private fun TemperatureUnitSettings(
    state: HTSServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
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
    onClickEvent: (DeviceConnectionViewEvent) -> Unit,
) {
    val listState = rememberLazyListState()
    val entries = no.nordicsemi.android.lib.profile.hts.TemperatureUnit.entries.map { it }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.hts_temperature_unit)) },
        text = {
            LazyColumn(
                state = listState
            ) {
                items(entries.size) { index ->
                    val entry = entries[index]
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onClickEvent(HTSViewEvent.OnTemperatureUnitSelected(entry))
                                onDismiss()
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.toString(),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.titleLarge,
                            color = if (state.temperatureUnit == entry)
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
