package no.nordicsemi.android.toolbox.libs.profile.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.toolbox.libs.core.data.GLSServiceData
import no.nordicsemi.android.toolbox.libs.core.data.common.WorkingMode
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSMeasurementContext
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSRecord
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RequestStatus
import no.nordicsemi.android.toolbox.libs.profile.data.glucoseConcentrationDisplayValue
import no.nordicsemi.android.toolbox.libs.profile.data.toDisplayString
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.GLSViewEvent.OnGLSRecordClick
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.GLSViewEvent.OnWorkingModeSelected
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
fun WorkingModeDropDown(
    isWorkingModeSelected: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    Column {
        OutlinedButton(onClick = { onExpand() }) {
            Row(
                modifier = Modifier.fillMaxWidth(0.5f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Working Mode",
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = "")
            }
        }
        if (isWorkingModeSelected)
            WorkingModeDialog(
                onDismiss = onDismiss,
            ) {
                onClickEvent(it)
                onDismiss()
            }
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkingModeDropDownPreview() {
    WorkingModeDropDown(false, {}, {}, {})
}

@Composable
private fun WorkingModeDialog(
    onDismiss: () -> Unit,
    onWorkingModeSelected: (DeviceConnectionViewEvent) -> Unit,
) {
    val listState = rememberLazyListState()
    val workingModeEntries = WorkingMode.entries.map { it }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(id = R.string.csc_dialog_title)) },
        text = {

            LazyColumn(
                state = listState
            ) {
                items(workingModeEntries.size) { index ->
                    val entry = workingModeEntries[index]
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onWorkingModeSelected(OnWorkingModeSelected(Profile.GLS, entry))
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.toDisplayString(),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.titleLarge,
//                            color = if (state.data.wheelSize.name == entry)
//                                MaterialTheme.colorScheme.primary else
//                                MaterialTheme.colorScheme.onBackground
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
internal fun GLSScreen(
    device: String,
    glsServiceData: GLSServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SettingsView(glsServiceData, onClickEvent)

        RecordsView(device, glsServiceData, onClickEvent)

    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ScreenSection {
            SectionTitle(
                resId = R.drawable.ic_gls,
                title = "Glucose Level",
                menu = {
                    var isWorkingModeClicked by rememberSaveable { mutableStateOf(false) }
                    WorkingModeDropDown(
                        isWorkingModeSelected = isWorkingModeClicked,
                        onExpand = { isWorkingModeClicked = true },
                        onDismiss = { isWorkingModeClicked = false },
                        onClickEvent = { onClickEvent(it) }
                    )
                }
            )

//            SectionRow {
//                KeyValueColumn(
//                    stringResource(id = R.string.hts_temperature),
//                    displayTemperature(
//                        htsServiceData.data.temperature,
//                        htsServiceData.temperatureUnit
//                    )
//                )
//                KeyValueColumnReverse(
//                    stringResource(id = R.string.hts_temperature_unit_title),
//                    "${htsServiceData.temperatureUnit}"
//                )
//            }
        }
    }
}

@Composable
private fun SettingsView(state: GLSServiceData, onEvent: (DeviceConnectionViewEvent) -> Unit) {
    ScreenSection {
        SectionTitle(icon = Icons.Default.Settings, title = "Request items")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (state.requestStatus == RequestStatus.PENDING) {
                CircularProgressIndicator()
            } else {
                WorkingMode.entries.forEach {
                    Button(onClick = { onEvent(OnWorkingModeSelected(Profile.GLS, it)) }) {
                        Text(it.toDisplayString())
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsViewPreview() {
    SettingsView(GLSServiceData()) {}
}

@Composable
private fun RecordsView(
    device: String,
    state: GLSServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    ScreenSection {
        if (state.records.isEmpty()) {
            RecordsViewWithoutData()
        } else {
            RecordsViewWithData(device, state, onClickEvent)
        }

    }
}

@Composable
private fun RecordsViewWithData(
    device: String,
    state: GLSServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(resId = R.drawable.ic_records, title = "Records")

        Spacer(modifier = Modifier.height(16.dp))

        state.records.keys.forEachIndexed { i, it ->
            RecordItem(device, it, state.records[it], onClickEvent)

            if (i < state.records.size - 1) {
                Spacer(modifier = Modifier.size(8.dp))
            }
        }
    }
}

@Composable
private fun RecordItem(
    device: String,
    record: GLSRecord,
    gleContext: GLSMeasurementContext?,
    onEvent: (DeviceConnectionViewEvent) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onEvent(OnGLSRecordClick(device, record, gleContext)) }
            .padding(8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            record.time?.let {
                Text(
                    text = stringResource(R.string.gls_timestamp, it),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = record.type.toDisplayString(),
                    style = MaterialTheme.typography.bodySmall
                )

                record.glucoseConcentration?.let { glucoseConcentration ->
                    record.unit?.let { unit ->
                        Text(
                            text = glucoseConcentrationDisplayValue(glucoseConcentration, unit),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordsViewWithoutData() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionTitle(icon = Icons.Default.Search, title = "No items")

        Text(
            text = stringResource(id = R.string.gls_no_records_info),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordsViewWithoutDataPreview() {
    RecordsViewWithoutData()
}

@Preview(showBackground = true)
@Composable
private fun RecordsViewWithDataPreview() {
    RecordsViewWithData(
        device = "RecordsViewWithData",
        state = GLSServiceData()
    ) {}
}