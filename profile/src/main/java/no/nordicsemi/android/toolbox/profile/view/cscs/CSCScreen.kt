package no.nordicsemi.android.toolbox.profile.view.cscs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import no.nordicsemi.android.lib.profile.csc.SpeedUnit
import no.nordicsemi.android.lib.profile.csc.WheelSizes
import no.nordicsemi.android.lib.profile.csc.WheelSizes.getWheelSizeByName
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.CSCServiceData
import no.nordicsemi.android.toolbox.profile.viewmodel.CSCEvent
import no.nordicsemi.android.ui.view.KeyValueColumn
import no.nordicsemi.android.ui.view.KeyValueColumnReverse
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionRow

@Composable
internal fun CSCScreen(
    serviceData: CSCServiceData,
    onClickEvent: (CSCEvent) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ScreenSection {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "\uD83D\uDEB4" + " Cycling",
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                CSCSettingView(serviceData, onClickEvent)
            }
            SensorsReadingView(state = serviceData, serviceData.speedUnit)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CSCScreenPreview() {
    CSCScreen(CSCServiceData()) { }
}

@Composable
private fun CSCSettingView(
    serviceData: CSCServiceData,
    onClickEvent: (CSCEvent) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var isWheelSizeClicked by rememberSaveable { mutableStateOf(false) }
        var isDropdownExpanded by rememberSaveable { mutableStateOf(false) }

        WheelSizeDropDown(
            state = serviceData,
            isWheelSizeClicked = isWheelSizeClicked,
            onExpand = { isWheelSizeClicked = true },
            onDismiss = { isWheelSizeClicked = false },
            onClickEvent = { onClickEvent(it) }
        )
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Speed unit settings",
            modifier = Modifier
                .clip(CircleShape)
                .size(28.dp)
                .clickable { isDropdownExpanded = true }
        )

        if (isDropdownExpanded)
            CSCSpeedSettingsFilterDropdown(
                serviceData,
                onDismiss = { isDropdownExpanded = false },
                onClickEvent = { onClickEvent(it) }
            )
    }
}

@Composable
private fun WheelSizeDropDown(
    state: CSCServiceData,
    isWheelSizeClicked: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onClickEvent: (CSCEvent) -> Unit
) {
    val wheelEntries = WheelSizes.data.map { it.name }
    Column {
        OutlinedButton(onClick = { onExpand() }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.csc_field_wheel_size),
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = "")
            }
        }
        if (isWheelSizeClicked)
            WheelSizeDialog(
                state = state,
                wheelSizeEntries = wheelEntries,
                onDismiss = onDismiss,
            ) {
                onClickEvent(CSCEvent.OnWheelSizeSelected(getWheelSizeByName(it)))
                onDismiss()
            }
    }
}

@Composable
private fun WheelSizeDialog(
    state: CSCServiceData,
    wheelSizeEntries: List<String>,
    onDismiss: () -> Unit,
    onWheelSizeSelected: (String) -> Unit,
) {
    val listState = rememberLazyListState()
    val selectedIndex = wheelSizeEntries.indexOf(state.data.wheelSize.name)

    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0) {
            listState.scrollToItem(selectedIndex)
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(id = R.string.csc_dialog_title)) },
        text = {
            LazyColumn(
                state = listState
            ) {
                items(wheelSizeEntries.size) { index ->
                    val entry = wheelSizeEntries[index]
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onWheelSizeSelected(entry)
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.titleLarge,
                            color = if (state.data.wheelSize.name == entry)
                                MaterialTheme.colorScheme.primary else
                                MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(
                    text = stringResource(id = no.nordicsemi.android.ui.R.string.cancel),
                )
            }
        }
    )
}

@Composable
private fun CSCSpeedSettingsFilterDropdown(
    state: CSCServiceData,
    onDismiss: () -> Unit,
    onClickEvent: (CSCEvent) -> Unit
) {
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
                    text = stringResource(R.string.csc_settings),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                Column {
                    SpeedUnit.entries.forEach { entry ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    onClickEvent(CSCEvent.OnSelectedSpeedUnitSelected(entry))
                                    onDismiss()
                                },
                        ) {
                            Text(
                                text = entry.displayName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                color = if (state.speedUnit == entry)
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
private fun CSCSpeedSettingsFilterDropdownPreview() {
    CSCSpeedSettingsFilterDropdown(
        state = CSCServiceData(),
        onDismiss = {},
        onClickEvent = {}
    )
}

@Composable
private fun SensorsReadingView(state: CSCServiceData, speedUnit: SpeedUnit) {
    val csc = state.data

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionRow {
            KeyValueColumn(
                stringResource(id = R.string.csc_field_speed),
                csc.displaySpeed(speedUnit)
            )
            KeyValueColumnReverse(
                stringResource(id = R.string.csc_field_cadence),
                csc.displayCadence()
            )
        }
        SectionRow {
            KeyValueColumn(
                stringResource(id = R.string.csc_field_distance),
                csc.displayDistance(speedUnit)
            )
            KeyValueColumnReverse(
                stringResource(id = R.string.csc_field_total_distance),
                csc.displayTotalDistance(speedUnit)
            )
        }
        Row {
            KeyValueColumn(
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
