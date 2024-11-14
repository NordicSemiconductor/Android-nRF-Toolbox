package no.nordicsemi.android.toolbox.libs.profile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.ui.view.RadioButtonGroup
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.core.data.CSCServiceData
import no.nordicsemi.android.toolbox.libs.core.data.csc.CSCData
import no.nordicsemi.android.toolbox.libs.core.data.csc.SpeedUnit
import no.nordicsemi.android.toolbox.libs.core.data.csc.WheelSize
import no.nordicsemi.android.toolbox.libs.core.data.csc.WheelSizes
import no.nordicsemi.android.toolbox.libs.profile.data.displayCadence
import no.nordicsemi.android.toolbox.libs.profile.data.displayDistance
import no.nordicsemi.android.toolbox.libs.profile.data.displayGearRatio
import no.nordicsemi.android.toolbox.libs.profile.data.displaySpeed
import no.nordicsemi.android.toolbox.libs.profile.data.displayTotalDistance
import no.nordicsemi.android.toolbox.libs.profile.data.temperatureSettingsItems
import no.nordicsemi.android.toolbox.libs.profile.data.toSpeedUnit
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.CSCViewEvent
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.ui.view.KeyValueField
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle
import no.nordicsemi.android.ui.view.dialog.FlowCanceled
import no.nordicsemi.android.ui.view.dialog.ItemSelectedResult
import no.nordicsemi.android.ui.view.dialog.StringListDialog
import no.nordicsemi.android.ui.view.dialog.StringListDialogConfig
import no.nordicsemi.android.ui.view.dialog.StringListDialogResult
import no.nordicsemi.android.ui.view.dialog.toAnnotatedString

@Composable
internal fun CSCScreen(
    serviceData: CSCServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit,
) {
    val showDialog = rememberSaveable { mutableStateOf(false) }
    if (showDialog.value) {
        val wheelEntries = WheelSizes.data.map { it.name }.toTypedArray()
        val wheelValues = WheelSizes.data.map { it.value }.toTypedArray()

        SelectWheelSizeDialog {
            when (it) {
                FlowCanceled -> showDialog.value = false
                is ItemSelectedResult -> {
                    onClickEvent(
                        CSCViewEvent.OnWheelSizeSelected(
                            WheelSize(wheelValues[it.index], wheelEntries[it.index])
                        )
                    )
                    showDialog.value = false
                }
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SettingsSection(serviceData.data, serviceData.speedUnit, onClickEvent) {
            showDialog.value = true
        }
        SensorsReadingView(state = serviceData, serviceData.speedUnit)
    }
}

@Composable
private fun WheelSizeView(state: CSCData, onClick: () -> Unit) {
    OutlinedButton(onClick = { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth(0.5f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(id = R.string.csc_field_wheel_size),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(text = state.wheelSize.name, style = MaterialTheme.typography.bodyMedium)
            }

            Icon(Icons.Default.ArrowDropDown, contentDescription = "")
        }
    }
}

@Preview
@Composable
private fun WheelSizeViewPreview() {
    WheelSizeView(CSCData()) { }
}

@Composable
private fun SettingsSection(
    state: CSCData,
    speedUnit: SpeedUnit,
    onEvent: (CSCViewEvent) -> Unit,
    onWheelButtonClick: () -> Unit,
) {
    ScreenSection {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionTitle(
                icon = Icons.Default.Settings,
                title = stringResource(R.string.csc_settings)
            )

            WheelSizeView(state, onWheelButtonClick)

            RadioButtonGroup(viewEntity = speedUnit.temperatureSettingsItems()) {
                onEvent(CSCViewEvent.OnSelectedSpeedUnitSelected(it.label.toSpeedUnit()))
            }
        }
    }
}

@Preview
@Composable
private fun ConnectedPreview() {
    CSCScreen(CSCServiceData()) { }
}

@Composable
internal fun SelectWheelSizeDialog(onEvent: (StringListDialogResult) -> Unit) {
    val wheelEntries = WheelSizes.data.map { it.name }.toTypedArray()

    StringListDialog(createConfig(wheelEntries) {
        onEvent(it)
    })
}

@Composable
private fun createConfig(
    entries: Array<String>,
    onResult: (StringListDialogResult) -> Unit
): StringListDialogConfig {
    return StringListDialogConfig(
        title = stringResource(id = R.string.csc_dialog_title).toAnnotatedString(),
        items = entries.toList(),
        onResult = onResult
    )
}

@Preview(showBackground = true)
@Composable
private fun SelectWheelSizeDialogPreview() {
    val wheelEntries = WheelSizes.data.map { it.value.toString() }.toTypedArray()
    StringListDialog(createConfig(wheelEntries) {})
}

@Composable
private fun SensorsReadingView(state: CSCServiceData, speedUnit: SpeedUnit) {
    val csc = state.data
    ScreenSection {
        SectionTitle(resId = R.drawable.ic_records, title = "Records")

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeyValueField(
                stringResource(id = R.string.csc_field_speed),
                csc.displaySpeed(speedUnit)
            )
            KeyValueField(
                stringResource(id = R.string.csc_field_cadence),
                csc.displayCadence()
            )
            KeyValueField(
                stringResource(id = R.string.csc_field_distance),
                csc.displayDistance(speedUnit)
            )
            KeyValueField(
                stringResource(id = R.string.csc_field_total_distance),
                csc.displayTotalDistance(speedUnit)
            )
            KeyValueField(
                stringResource(id = R.string.csc_field_gear_ratio),
                csc.displayGearRatio()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SensorsReadingViewPreview() {
    SensorsReadingView(CSCServiceData(), SpeedUnit.KM_H)
}