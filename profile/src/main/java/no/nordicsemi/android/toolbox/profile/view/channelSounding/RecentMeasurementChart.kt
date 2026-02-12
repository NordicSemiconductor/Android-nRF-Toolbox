package no.nordicsemi.android.toolbox.profile.view.channelSounding

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import no.nordicsemi.android.common.theme.NordicTheme

private const val X_AXIS_ELEMENTS_COUNT = 40.0f

private val customBlue = "#00A9CE".toColorInt()
private val backgroundColor = "#F5F5F5".toColorInt()

@Composable
internal fun RecentMeasurementChart(previousData: List<Float>) {
    val items = previousData.takeLast(X_AXIS_ELEMENTS_COUNT.toInt()).reversed()
    val isSystemInDarkTheme = isSystemInDarkTheme()
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { createLineChartView(isSystemInDarkTheme, it, items) },
        update = { updateData(items, it) }
    )
}

/**
 * Processes raw data into Chart Entries and Segment Colors.
 * This ensures the logic is identical for both initial load and updates.
 */
private fun prepareChartData(points: List<Float>): Triple<List<Entry>, List<Int>, List<Int>> {
    val entries = points.mapIndexed { i, v -> Entry(-i.toFloat(), v) }.reversed()

    val segmentColors = mutableListOf<Int>()
    for (i in 0 until points.size - 1) {
        if (points[i] == 0.0f || points[i + 1] == 0.0f) {
            segmentColors.add(Color.TRANSPARENT)
        } else {
            segmentColors.add(customBlue)
        }
    }

    val circleColors = points.map {
        if (it == 0.0f) Color.TRANSPARENT else customBlue
    }.reversed()

    return Triple(entries, segmentColors.reversed(), circleColors)
}

internal fun createLineChartView(
    isDarkTheme: Boolean,
    context: Context,
    points: List<Float>
): LineChart {
    return LineChart(context).apply {
        // 1. General Configuration
        description.isEnabled = false
        setTouchEnabled(false)
        setDrawGridBackground(false)
        isDragEnabled = false
        setScaleEnabled(false)
        setPinchZoom(false)

        // 2. Theme Styling
        val contentColor = if (isDarkTheme) Color.WHITE else Color.BLACK
        setBackgroundColor(if (isDarkTheme) Color.TRANSPARENT else backgroundColor)

        xAxis.apply {
            enableGridDashedLine(10f, 10f, 0f)
            axisMinimum = -X_AXIS_ELEMENTS_COUNT
            axisMaximum = 0f
            setAvoidFirstLastClipping(true)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            setDrawGridLines(false)
            gridColor = contentColor
            textColor = contentColor
        }

        axisLeft.apply {
            enableGridDashedLine(10f, 10f, 0f)
            gridColor = contentColor
            textColor = contentColor
        }
        axisRight.isEnabled = false

        // 3. Custom Legend (Fixed to prevent multiple dashed lines)
        legend.apply {
            isEnabled = true
            textColor = customBlue
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            setCustom(
                listOf(
                LegendEntry().apply {
                    label = "Recent Measurements"
                    form = Legend.LegendForm.LINE
                    formColor = customBlue
                    formLineWidth = 2f
                    formSize = 15f
                }
            ))
        }

        // 4. Initial Data Binding
        val (entries, colors) = prepareChartData(points)
        val set1 = LineDataSet(entries, "Recent Measurements").apply {
            setDrawIcons(false)
            setDrawValues(false)
            setDrawCircles(false)
            this.colors = colors // Apply segment colors
            lineWidth = 3f
            valueTextSize = 9f
        }

        data = LineData(set1)
    }
}

private fun updateData(points: List<Float>, chart: LineChart) {
    val (entries, colors) = prepareChartData(points)

    chart.data?.let { lineData ->
        if (lineData.dataSetCount > 0) {
            val set1 = lineData.getDataSetByIndex(0) as LineDataSet
            set1.values = entries
            set1.colors = colors // Update segments

            set1.notifyDataSetChanged()
            lineData.notifyDataChanged()
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LineChartView_Preview() {
    NordicTheme {
        RecentMeasurementChart(
            previousData = listOf(
                3.2f,
                4.5f,
                2.8f,
                5.0f,
                3.6f,
                4.1f,
                0.0f,
                0.0f,
                0.0f,
                3.9f,
                4.8f,
                2.5f,
                3.3f,
                4.0f,
                3.7f,
                0.0f,
                0.0f,
                4.2f,
                0.0f,
                0.0f,
                0.0f,
                3.0f,
                3.3f,
                4.0f,
                3.7f,
                0.0f,
                0.0f,
                0.0f,
                3.2f,
                4.5f,
                2.8f,
                5.0f,
                3.6f,
                4.1f,
            )
        )
    }
}
