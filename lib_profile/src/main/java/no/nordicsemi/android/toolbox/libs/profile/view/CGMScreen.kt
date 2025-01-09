package no.nordicsemi.android.toolbox.libs.profile.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.toolbox.libs.core.data.CGMRecordWithSequenceNumber
import no.nordicsemi.android.toolbox.libs.core.data.CGMServiceData
import no.nordicsemi.android.toolbox.libs.core.data.common.WorkingMode
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RequestStatus
import no.nordicsemi.android.toolbox.libs.profile.data.formattedTime
import no.nordicsemi.android.toolbox.libs.profile.data.glucoseConcentration
import no.nordicsemi.android.toolbox.libs.profile.data.toDisplayString
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.GLSViewEvent.OnWorkingModeSelected
import no.nordicsemi.android.ui.view.KeyValueColumn
import no.nordicsemi.android.ui.view.KeyValueColumnReverse
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionRow
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun CGMScreen(
    serviceData: CGMServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    var isWorkingModeClicked by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ScreenSection {
            SectionTitle(
                resId = R.drawable.ic_cgm,
                title = "Continuous glucose monitoring",
                menu = {
                    WorkingModeDropDown(
                        cgmState = serviceData,
                        isWorkingModeSelected = isWorkingModeClicked,
                        onExpand = { isWorkingModeClicked = true },
                        onDismiss = { isWorkingModeClicked = false },
                        onClickEvent = { onClickEvent(it) }
                    )
                }
            )
        }
        RecordsView(serviceData)
    }
}

@Composable
private fun WorkingModeDropDown(
    cgmState: CGMServiceData,
    isWorkingModeSelected: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    if (cgmState.requestStatus == RequestStatus.PENDING) {
        CircularProgressIndicator()
    } else {
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
            if (isWorkingModeSelected)
                WorkingModeDialog(
                    cgmState = cgmState,
                    onDismiss = onDismiss,
                ) {
                    onClickEvent(it)
                    onDismiss()
                }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkingModeDropDownPreview() {
    WorkingModeDropDown(CGMServiceData(), false, {}, {}, {})
}

@Composable
private fun WorkingModeDialog(
    cgmState: CGMServiceData,
    onDismiss: () -> Unit,
    onWorkingModeSelected: (DeviceConnectionViewEvent) -> Unit,
) {
    val listState = rememberLazyListState()
    val workingModeEntries = WorkingMode.entries.map { it }
    val selectedIndex = workingModeEntries.indexOf(cgmState.workingMode)

    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0) {
            listState.scrollToItem(selectedIndex)
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Select working mode") },
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
                                onWorkingModeSelected(OnWorkingModeSelected(Profile.CGM, entry))
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.toDisplayString(),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.titleLarge,
                            color = if ((cgmState.workingMode == entry) && cgmState.records.isNotEmpty()) {
                                MaterialTheme.colorScheme.primary
                            } else
                                MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun RecordsView(state: CGMServiceData) {
    ScreenSection {
        if (state.records.isEmpty()) {
            RecordsViewWithoutData()
        } else {
            RecordsViewWithData(state)
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
            text = stringResource(R.string.cgms_no_records_info),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordsViewWithoutDataPreview() {
    RecordsViewWithoutData()
}

@Composable
private fun RecordsViewWithData(state: CGMServiceData) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        SectionTitle(resId = R.drawable.ic_records, title = "Records")
        val newRecord = when (state.workingMode) {
            WorkingMode.ALL -> state.records
            WorkingMode.LAST -> listOf(state.records.last())
            WorkingMode.FIRST -> listOf(state.records.first())
            null -> state.records
        }

        newRecord.forEachIndexed { i, it ->
            RecordItem(it)

            if (i < newRecord.size - 1) {
                HorizontalDivider()
                Spacer(modifier = Modifier.size(8.dp))
            }
        }
    }
}

@Composable
private fun RecordItem(record: CGMRecordWithSequenceNumber) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            SectionRow {
                KeyValueColumn(
                    value = stringResource(id = R.string.cgms_sequence_number),
                    key = record.sequenceNumber.toString()
                )
                KeyValueColumnReverse(
                    value = "Date/Time",
                    key = record.formattedTime()
                )
            }
            SectionRow {
                KeyValueColumn(
                    "Glucose concentration",
                    record.glucoseConcentration()
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordsViewWithDataPreview() {
    RecordsViewWithData(
        state = CGMServiceData()
    )
}
