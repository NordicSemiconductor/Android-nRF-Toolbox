package no.nordicsemi.android.toolbox.libs.profile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import no.nordicsemi.android.toolbox.libs.core.data.GLSServiceData
import no.nordicsemi.android.toolbox.libs.core.data.common.WorkingMode
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.Carbohydrate
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.ConcentrationUnit
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSMeasurementContext
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSRecord
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.Health
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.Meal
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.Medication
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.MedicationUnit
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RecordType
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RequestStatus
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.Tester
import no.nordicsemi.android.toolbox.libs.profile.data.glucoseConcentrationDisplayValue
import no.nordicsemi.android.toolbox.libs.profile.data.toDisplayString
import no.nordicsemi.android.toolbox.libs.profile.view.gls.details.GLSDetails
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.GLSViewEvent.OnWorkingModeSelected
import no.nordicsemi.android.ui.view.KeyValueColumn
import no.nordicsemi.android.ui.view.KeyValueColumnReverse
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionRow
import no.nordicsemi.android.ui.view.SectionTitle
import java.util.Calendar

@Composable
internal fun GLSScreen(
    glsServiceData: GLSServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    var isWorkingModeClicked by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ScreenSection {
            SectionTitle(
                resId = R.drawable.ic_gls,
                title = "Glucose level",
                menu = {
                    WorkingModeDropDown(
                        glsState = glsServiceData,
                        isWorkingModeSelected = isWorkingModeClicked,
                        onExpand = { isWorkingModeClicked = true },
                        onDismiss = { isWorkingModeClicked = false },
                        onClickEvent = { onClickEvent(it) }
                    )
                }
            )
        }
        RecordsView(glsServiceData)
    }
}

@Preview(showBackground = true)
@Composable
private fun GLSScreenPreview() {
    GLSScreen(
        glsServiceData = GLSServiceData()
    ) {}
}

@Composable
private fun WorkingModeDropDown(
    glsState: GLSServiceData,
    isWorkingModeSelected: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    if (glsState.requestStatus == RequestStatus.PENDING) {
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
                    glsState = glsState,
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
    WorkingModeDropDown(GLSServiceData(), false, {}, {}, {})
}

@Composable
private fun WorkingModeDialog(
    glsState: GLSServiceData,
    onDismiss: () -> Unit,
    onWorkingModeSelected: (DeviceConnectionViewEvent) -> Unit,
) {
    val listState = rememberLazyListState()
    val workingModeEntries = WorkingMode.entries.map { it }
    val selectedIndex = workingModeEntries.indexOf(glsState.workingMode)

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
                                onWorkingModeSelected(OnWorkingModeSelected(Profile.GLS, entry))
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.toDisplayString(),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.titleLarge,
                            color = if ((glsState.workingMode == entry) && glsState.records.isNotEmpty()) {
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

@Preview(showBackground = true)
@Composable
private fun WorkingModeDialogPreview() {
    WorkingModeDialog(GLSServiceData(workingMode = WorkingMode.ALL), {}) {}
}

@Composable
private fun RecordsView(
    state: GLSServiceData
) {
    ScreenSection {
        if (state.records.isEmpty()) {
            RecordsViewWithoutData()
        } else {
            RecordsViewWithData(state)
        }
    }
}

@Composable
private fun RecordsViewWithData(
    state: GLSServiceData
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(resId = R.drawable.ic_records, title = "Records")

        state.records.keys.forEachIndexed { i, it ->
            RecordItem(it, state.records[it])

            if (i < state.records.size - 1) {
                HorizontalDivider()
                Spacer(modifier = Modifier.size(8.dp))
            }
        }
    }
}

@Composable
private fun RecordItem(
    record: GLSRecord,
    gleContext: GLSMeasurementContext?
) {
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                showBottomSheet = true
            }
            .padding(8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            SectionRow {
                record.glucoseConcentration?.let { glucoseConcentration ->
                    record.unit?.let { unit ->
                        glucoseConcentrationDisplayValue(glucoseConcentration, unit)
                    }
                }?.let {
                    KeyValueColumn(
                        record.type.toDisplayString(),
                        it
                    )
                }
                record.time?.let {
                    KeyValueColumnReverse(
                        value = "Time",
                        key = stringResource(R.string.gls_timestamp, it)
                    )
                }
            }
        }
    }

    if (showBottomSheet) {
        GLSDetailsBottomSheet(record, gleContext) { showBottomSheet = false }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GLSDetailsBottomSheet(
    record: GLSRecord,
    context: GLSMeasurementContext?,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 16.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .width(50.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    ) {
        GLSDetails(record, context)
    }
}


@Preview(showBackground = true)
@Composable
private fun RecordItemPreview() {
    RecordItem(
        record = GLSRecord(
            sequenceNumber = 1,
            time = Calendar.getInstance(),
            glucoseConcentration = 0.5f,
            unit = ConcentrationUnit.UNIT_KGPL,
            type = RecordType.VENOUS_PLASMA,
            status = null,
            sampleLocation = null,
            contextInformationFollows = true
        ),
        gleContext = GLSMeasurementContext(
            sequenceNumber = 20,
            carbohydrate = Carbohydrate.LUNCH,
            carbohydrateAmount = 12.5f,
            meal = Meal.CASUAL,
            tester = Tester.SELF,
            health = Health.NO_HEALTH_ISSUES,
            exerciseDuration = 2,
            exerciseIntensity = 1,
            medication = Medication.PRE_MIXED_INSULIN,
            medicationQuantity = .5f,
            medicationUnit = MedicationUnit.UNIT_MG,
            HbA1c = 0.5f
        ),
    )
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
