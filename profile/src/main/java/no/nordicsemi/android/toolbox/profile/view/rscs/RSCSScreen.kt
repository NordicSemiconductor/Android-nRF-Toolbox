package no.nordicsemi.android.toolbox.profile.view.rscs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.lib.profile.rscs.RSCFeatureData
import no.nordicsemi.android.lib.profile.rscs.RSCSSettingsUnit
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.RSCSServiceData
import no.nordicsemi.android.toolbox.profile.viewmodel.ProfileUiEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.RSCSEvent
import no.nordicsemi.android.ui.view.FeatureSupported
import no.nordicsemi.android.ui.view.KeyValueColumn
import no.nordicsemi.android.ui.view.KeyValueColumnReverse
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionRow
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun RSCSScreen(
    serviceData: RSCSServiceData,
    onClickEvent: (ProfileUiEvent) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ScreenSection(modifier = Modifier.padding(bottom = 16.dp)) {
            SectionTitle(
                resId = R.drawable.ic_rscs,
                title = if (serviceData.data.running) "Running" else "Walking",
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                menu = { RSCSSettingsDropdown(serviceData, onClickEvent) }
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
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
            serviceData.feature?.let {
                HorizontalDivider()
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                ) {
                    Text("Supported features", style = MaterialTheme.typography.titleMedium)
                    if (it.instantaneousStrideLengthMeasurementSupported) {
                        FeatureSupported(
                            stringResource(id = R.string.instantaneous_stride_length_measurement)
                        )
                    }
                    if (it.totalDistanceMeasurementSupported) {
                        FeatureSupported(
                            stringResource(id = R.string.total_distance_measurement)
                        )
                    }
                    if (it.walkingOrRunningStatusSupported) {
                        FeatureSupported(
                            stringResource(id = R.string.walking_or_running_status)
                        )
                    }
                    if (it.calibrationSupported) {
                        FeatureSupported(stringResource(id = R.string.calibration))
                    }
                    if (it.multipleSensorLocationsSupported) {
                        FeatureSupported(stringResource(id = R.string.multiple_sensor_location))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RSCSScreenPreview() {
    RSCSScreen(
        RSCSServiceData(
            feature = RSCFeatureData(
                instantaneousStrideLengthMeasurementSupported = true,
                totalDistanceMeasurementSupported = true,
                walkingOrRunningStatusSupported = true,
                calibrationSupported = true,
                multipleSensorLocationsSupported = true
            )
        )
    ) {}
}

@Composable
private fun RSCSSettingsDropdown(
    state: RSCSServiceData,
    onClickEvent: (ProfileUiEvent) -> Unit
) {
    var openSettingsDialog by rememberSaveable { mutableStateOf(false) }

    Column {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "display settings",
            modifier = Modifier
                .clip(CircleShape)
                .clickable { openSettingsDialog = true }
        )

        if (openSettingsDialog) {
            RSCSSettingsDialog(state, { openSettingsDialog = false }, onClickEvent)
        }
    }
}

@Composable
private fun RSCSSettingsDialog(
    state: RSCSServiceData,
    onDismiss: () -> Unit,
    onSpeedUnitSelected: (ProfileUiEvent) -> Unit
) {
    val speedUnitEntries = RSCSSettingsUnit.entries.map { it }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(R.string.rscs_settings_unit_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                speedUnitEntries.forEachIndexed { _, entry ->
                    Text(
                        text = entry.toString(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSpeedUnitSelected(
                                    RSCSEvent.OnSelectedSpeedUnitSelected(
                                        entry
                                    )
                                )
                                onDismiss()
                            },
                        color = if (state.unit == entry)
                            MaterialTheme.colorScheme.primary else
                            MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },
        confirmButton = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun RSCSSettingsDialogPreview() {
    RSCSSettingsDialog(
        state = RSCSServiceData(),
        onDismiss = {},
        onSpeedUnitSelected = {}
    )
}
