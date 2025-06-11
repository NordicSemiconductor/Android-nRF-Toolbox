package no.nordicsemi.android.toolbox.profile.view.cgms

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
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

    GlucoseChartScreen()
//    if (state.records.isNotEmpty()) {
////        val data = generateMockData()
//    }
    /*  if (state.records.isNotEmpty()) {
          LineChartView(state, false)
      }*/
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

/** The number of elements to display on the X axis. */
private const val X_AXIS_ELEMENTS_COUNT = 40f

/** The minimum value for the Y axis. */
private const val AXIS_MIN = 0

/** The maximum value for the Y axis. */
private const val AXIS_MAX = 50

private fun createLineChartView(
    isDarkTheme: Boolean,
    context: Context,
    ourData: List<Pair<Int, Int>>,
    zoomIn: Boolean
): LineChart {
    return LineChart(context).apply {
        description.isEnabled = false
        legend.isEnabled = false
        setTouchEnabled(true) // Enable touch gestures
        setDrawGridBackground(false)
        isDragEnabled = true
        setScaleEnabled(true) // Enable scaling
        setPinchZoom(true) // Enable pinch zoom

        if (isDarkTheme) {
            setBackgroundColor(Color.TRANSPARENT)
            xAxis.gridColor = Color.WHITE
            xAxis.textColor = Color.WHITE
            axisLeft.gridColor = Color.WHITE
            axisLeft.textColor = Color.WHITE
        } else {
            setBackgroundColor(Color.WHITE)
            xAxis.gridColor = Color.BLACK
            xAxis.textColor = Color.BLACK
            axisLeft.gridColor = Color.BLACK
            axisLeft.textColor = Color.BLACK
        }

        xAxis.apply {
            enableGridDashedLine(10f, 10f, 0f)
            axisMinimum = 0f
            axisMaximum = X_AXIS_ELEMENTS_COUNT
            setAvoidFirstLastClipping(true)
            position = XAxis.XAxisPosition.BOTTOM
//            setDrawLabels(false) // Hide X-axis labels
            setDrawGridLines(false) // Hide vertical grid lines
        }
        axisLeft.apply {
            enableGridDashedLine(10f, 10f, 0f)
            axisMaximum = ourData.maxOfOrNull { it.second }?.toFloat() ?: 0f
            axisMinimum = ourData.minOfOrNull { it.second }?.toFloat() ?: 0f
//            setDrawGridLines(true) // Show horizontal grid lines
        }
        axisRight.isEnabled = false

        val entries = ourData.map {
            Entry(it.first.toFloat(), it.second.toFloat())
        }
        // create a dataset and give it a type
        if (data != null && data.dataSetCount > 0) {
            val set1 = data!!.getDataSetByIndex(0) as LineDataSet
            set1.values = entries
            set1.notifyDataSetChanged()
            data!!.notifyDataChanged()
            notifyDataSetChanged()
        } else {
            val set1 = LineDataSet(entries, "DataSet 1")

            set1.setDrawIcons(false)
            set1.setDrawValues(false)

            // solid line
            set1.enableDashedLine(0f, 0f, 0f)

            // red line and points
            set1.color = Color.RED
            set1.setCircleColor(Color.RED)

            // line thickness and point size
            set1.lineWidth = 1f
            set1.circleRadius = 2f

            // draw points as solid circles
            set1.setDrawCircleHole(false)

            // customize legend entry
            set1.formLineWidth = 1f
            set1.formSize = 15f

            // text size of values
            set1.valueTextSize = 9f

            val dataSets = ArrayList<ILineDataSet>()
            dataSets.add(set1) // add the data sets

            // create a data object with the data sets
            val data = LineData(dataSets)

            // set data
            setData(data)
            setVisibleXRangeMaximum(10f)
            moveViewToX(0f)
        }
    }
}

private fun updateData(
    isDarkTheme: Boolean,
    points: List<Pair<Int, Int>>,
    chart: LineChart,
    zoomIn: Boolean
) {
    val entries = points.map {
        Entry(it.first.toFloat(), it.second.toFloat())
    }

    with(chart) {
        axisLeft.apply {
            axisMaximum = points.maxOfOrNull { it.second }?.toFloat() ?: 0f
            axisMinimum = points.minOfOrNull { it.second }?.toFloat() ?: 0f
        }
        xAxis.axisMaximum = points.size.toFloat() // Update axisMaximum to the size of heart rates
        if (data != null && data.dataSetCount > 0) {
            val set1 = data!!.getDataSetByIndex(0) as LineDataSet
            set1.values = entries
            set1.notifyDataSetChanged()
            data!!.notifyDataChanged()
            notifyDataSetChanged()
            invalidate()
        } else {
            val set1 = LineDataSet(entries, "DataSet 1")
            set1.setDrawIcons(false)
            set1.setDrawValues(false)
            set1.enableDashedLine(10f, 5f, 0f)
            set1.color = if (isDarkTheme) Color.WHITE else Color.BLACK
            set1.setCircleColor(if (isDarkTheme) Color.WHITE else Color.BLACK)
            set1.lineWidth = 1f
            set1.circleRadius = 2f
            set1.setDrawCircleHole(false)
            set1.formLineWidth = 1f
            set1.formSize = 15f
            set1.valueTextSize = 9f

            val dataSets = ArrayList<ILineDataSet>().apply { add(set1) }
            val data = LineData(dataSets)
            setData(data)
        }
        setVisibleXRangeMaximum(40f)
        moveViewToX(entries.size.toFloat())
    }
}

@Composable
private fun LineChartView(state: CGMServiceData, zoomIn: Boolean) {
    val items = state.records.map {
        Pair(it.sequenceNumber, it.record.glucoseConcentration.toInt())
    }
    val isSystemInDarkTheme = isSystemInDarkTheme()
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { createLineChartView(isSystemInDarkTheme, it, items, zoomIn) },
        update = { updateData(isSystemInDarkTheme, items, it, zoomIn) }
    )
}
