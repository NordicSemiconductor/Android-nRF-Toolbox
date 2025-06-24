package no.nordicsemi.android.toolbox.profile.view.cgms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import no.nordicsemi.android.lib.profile.cgms.data.CGMRecord
import no.nordicsemi.android.lib.profile.cgms.data.CGMStatus
import no.nordicsemi.android.lib.profile.common.WorkingMode
import no.nordicsemi.android.lib.profile.gls.data.RequestStatus
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.CGMRecordWithSequenceNumber
import no.nordicsemi.android.toolbox.profile.data.CGMServiceData
import no.nordicsemi.android.toolbox.profile.data.Profile
import no.nordicsemi.android.toolbox.profile.view.gls.toDisplayString
import no.nordicsemi.android.toolbox.profile.viewmodel.GLSEvent.OnWorkingModeSelected
import no.nordicsemi.android.toolbox.profile.viewmodel.ProfileUiEvent
import no.nordicsemi.android.ui.view.KeyValueColumn
import no.nordicsemi.android.ui.view.KeyValueColumnReverse
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionRow
import no.nordicsemi.android.ui.view.SectionTitle
import java.util.Calendar

@Composable
internal fun CGMScreen(
    serviceData: CGMServiceData,
    onClickEvent: (ProfileUiEvent) -> Unit
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
    onClickEvent: (ProfileUiEvent) -> Unit
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
                    Text(
                        text = if (cgmState.workingMode != null) {
                            cgmState.workingMode!!.toDisplayString()
                        } else {
                            "Request"
                        }
                    )
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
    onWorkingModeSelected: (ProfileUiEvent) -> Unit,
) {
    val listState = rememberLazyListState()
    val workingModeEntries = no.nordicsemi.android.lib.profile.common.WorkingMode.entries.map { it }
    val selectedIndex = workingModeEntries.indexOf(cgmState.workingMode)

    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0) {
            listState.scrollToItem(selectedIndex)
        }
    }

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
                    text = "Request record",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                LazyColumn(
                    state = listState
                ) {
                    items(workingModeEntries.size) { index ->
                        val entry = workingModeEntries[index]
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    onWorkingModeSelected(OnWorkingModeSelected(Profile.CGM, entry))
                                }
                                .padding(8.dp),
                        ) {
                            Text(
                                text = entry.toDisplayString(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                style = MaterialTheme.typography.titleLarge,
                                color = if ((cgmState.workingMode == entry) && cgmState.records.isNotEmpty()) {
                                    MaterialTheme.colorScheme.primary
                                } else
                                    MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    }
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        SectionTitle(resId = R.drawable.ic_records, title = "Records")

        val newRecord = when (state.workingMode) {
            WorkingMode.ALL -> state.records
            WorkingMode.LAST -> listOf(state.records.last())
            WorkingMode.FIRST -> listOf(state.records.first())
            null -> state.records
        }

        // Max height for the scrollable section, adjust as needed (e.g. 300.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column {
                newRecord.forEachIndexed { i, it ->
                    RecordItem(it)
                    if (i < newRecord.size - 1) {
                        HorizontalDivider()
                    }
                }
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
                    "Glucose concentration",
                    record.glucoseConcentration(),
                    keyStyle = MaterialTheme.typography.titleMedium
                )
            }
            SectionRow {
                KeyValueColumn(
                    value = "Date/Time",
                    key = record.formattedTime()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordsViewWithDataPreview() {
    RecordsViewWithData(
        state = CGMServiceData(
            records = listOf(
                CGMRecordWithSequenceNumber(
                    sequenceNumber = 12,
                    record = CGMRecord(
                        glucoseConcentration = 0.5f,
                        trend = 2.05f,
                        quality = 0.5f,
                        status = CGMStatus(2, 3, 5),
                        timeOffset = 2,
                        crcPresent = true
                    ),
                    timestamp = Calendar.TUESDAY.toLong()
                ),
                CGMRecordWithSequenceNumber(
                    sequenceNumber = 13,
                    record = CGMRecord(
                        glucoseConcentration = 0.5f,
                        trend = 2.05f,
                        quality = 0.5f,
                        status = CGMStatus(2, 3, 5),
                        timeOffset = 2,
                        crcPresent = true
                    ),
                    timestamp = Calendar.TUESDAY.toLong()
                )
            )
        )

    )
}
