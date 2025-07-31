package no.nordicsemi.android.toolbox.profile.view.cgms

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import no.nordicsemi.android.toolbox.profile.data.uart.MacroEol
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class GlucoseEntry(
    val timestamp: Long,   // epoch millis
    val value: Float       // glucose value (mg/dL or mmol/L)
)

enum class TimeRange(val days: Int?) {
    DAY(1),
    WEEK(7),
    MONTH(30),
    YEAR(365),
    MAX(null)
}

fun filterEntries(entries: List<GlucoseEntry>, timeRange: TimeRange): List<GlucoseEntry> {
    if (timeRange == TimeRange.MAX) return entries

    val now = System.currentTimeMillis()
    val cutoff = now - (timeRange.days!! * 24 * 60 * 60 * 1000L)
    return entries.filter { it.timestamp >= cutoff }
}

fun toLineDataSet(filteredEntries: List<GlucoseEntry>): LineDataSet {
    val entries = filteredEntries.map {
        Entry(it.timestamp.toFloat(), it.value)
    }

    return LineDataSet(entries, "Glucose").apply {
        color = Color.BLUE
        setDrawValues(false)
        setDrawCircles(false)
        lineWidth = 2f
        mode = LineDataSet.Mode.LINEAR
    }
}

class TimeValueFormatter(private val chart: LineChart) : ValueFormatter() {
    private val minuteFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val hourFormatter = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
    private val dayFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
    private val monthFormatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    override fun getFormattedValue(value: Float): String {
        val time = Date(value.toLong() * 1000) // If your x-axis is in seconds

        val range = chart.viewPortHandler.scaleX

        return when {
            range > 30 -> minuteFormatter.format(time)
            range > 10 -> hourFormatter.format(time)
            range > 2 -> dayFormatter.format(time)
            else -> monthFormatter.format(time)
        }
    }
}

class TimestampAxisFormatter : ValueFormatter() {
    private val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

    override fun getFormattedValue(value: Float): String {
        return sdf.format(Date(value.toLong()))
    }
}

fun updateChart(chart: LineChart, allData: List<GlucoseEntry>, range: TimeRange) {
    val filtered = filterEntries(allData, range)
    val dataSet = toLineDataSet(filtered)
    val glucoseValues = allData.map { it.value }
    val minY = (glucoseValues.minOrNull() ?: 0f) - 10f
    val maxY = (glucoseValues.maxOrNull() ?: 0f) + 10f
    chart.axisLeft.apply {
        axisMinimum = minY
        axisMaximum = maxY
        isEnabled = true
        setDrawLabels(true)
        setDrawGridLines(true)
        setDrawAxisLine(true)
    }

    chart.axisRight.apply {
        axisMinimum = minY
        axisMaximum = maxY
        isEnabled = true
    }
    chart.data = LineData(dataSet)
    chart.invalidate()
}

@Composable
fun GlucoseChartScreen() {
    val mockData = remember { generateMockData() }
    var selectedRange by remember { mutableStateOf(TimeRange.DAY) }

    val filteredData = remember(selectedRange, mockData) {
        filterEntries(mockData, selectedRange)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .height(600.dp) // Try fixed height to debug
    ) {
        TimeRangeSelector(selectedRange) { selectedRange = it }
        Spacer(modifier = Modifier.height(16.dp))
        GlucoseLineChart(entries = filteredData)
    }
}

@Composable
private fun TimeRangeSelector(selected: TimeRange, onSelect: (TimeRange) -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            TimeRange.entries.forEachIndexed { index, timeRange ->
                val isSelectedRange = timeRange == selected
                val clip =
                    if (isSelectedRange) RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)
                val (color, textColor) = if (isSelectedRange) {
                    MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(clip)
                        .background(color = color)
                        .clickable { onSelect(timeRange) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = timeRange.name,
                        color = textColor
                    )
                }
                if ((index < MacroEol.entries.size - 1) && !isSelectedRange)
                    VerticalDivider(
                        modifier = Modifier
                            .height(IntrinsicSize.Max)
                            .background(MaterialTheme.colorScheme.onSurface)
                    )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GlucoseChartScreenPreview() {
    TimeRangeSelector(
        selected = TimeRange.DAY,
        onSelect = {}
    )
}

@Composable
fun GlucoseLineChart(entries: List<GlucoseEntry>) {

    AndroidView(
        factory = { ctx ->
            LineChart(ctx).apply {
                description.isEnabled = true
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)

                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.valueFormatter = TimeValueFormatter(this)

                axisRight.isEnabled = true
                legend.isEnabled = true
                setVisibleXRangeMaximum(10f)
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { chart ->

            val lineEntries = entries.map { Entry(it.timestamp.toFloat(), it.value) }

            chart.xAxis.granularity = 1f // Set granularity to 1 second for better readability
            val dataSet = LineDataSet(lineEntries, "Glucose").apply {
                color = Color.BLUE
                lineWidth = 1f
                setDrawValues(false)
                setDrawCircles(false)
                mode = LineDataSet.Mode.LINEAR

                setDrawIcons(false)
                enableDashedLine(10f, 5f, 0f)
                circleRadius = 2f
                setDrawCircleHole(false)
                formLineWidth = 1f
                valueTextSize = 9f
                formSize = 15f
            }

            updateChart(
                chart,
                entries,
                TimeRange.DAY // Default to DAY, can be changed based on user selection
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun GlucoseLineChartPreview() {
    // Generate mock data for preview
    val mockData = generateMockData()
    GlucoseLineChart(entries = mockData)
}


fun generateMockData(): List<GlucoseEntry> {
    val now = System.currentTimeMillis()
    val data = mutableListOf<GlucoseEntry>()
    val totalDays = 400
    val interval = 60 * 60 * 1000L // every hour

    for (i in 0..(24 * totalDays)) {
        val timestamp = now - (i * interval)
        val glucose = Random.nextInt(80, 180).toFloat()
        data.add(GlucoseEntry(timestamp, glucose))
    }

    return data.sortedBy { it.timestamp }
}

