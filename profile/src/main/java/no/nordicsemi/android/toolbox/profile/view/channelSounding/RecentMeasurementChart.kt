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
private val signalLostColor = "#00A9CE".toColorInt()

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
 * @return Pair<LineDataSet, LineDataSet>: one for solid segments, one for dashed bridges.
 */
private fun prepareTwoDataSets(points: List<Float>): Pair<LineDataSet, LineDataSet> {
    val adjustedPoints = mutableListOf<Float>()
    var lastValidValue = points.firstOrNull { it != 0.0f } ?: 0.0f

    points.forEach { value ->
        if (value == 0.0f) adjustedPoints.add(lastValidValue)
        else {
            adjustedPoints.add(value)
            lastValidValue = value
        }
    }

    val entries = adjustedPoints.mapIndexed { i, v -> Entry(-i.toFloat(), v) }.reversed()

    // SOLID BLUE DATASET
    val solidColors = mutableListOf<Int>()
    for (i in 0 until points.size - 1) {
        if (points[i] != 0.0f && points[i + 1] != 0.0f) solidColors.add(customBlue)
        else solidColors.add(Color.TRANSPARENT)
    }

    val solidSet = LineDataSet(entries, "Valid Data").apply {
        lineWidth = 3f
        setDrawValues(false)
        setDrawCircles(false)
        colors = solidColors.reversed()
    }

    // DASHED DATASET
    val dashedColors = mutableListOf<Int>()
    for (i in 0 until points.size - 1) {
        if (points[i] == 0.0f || points[i + 1] == 0.0f) dashedColors.add(signalLostColor)
        else dashedColors.add(Color.TRANSPARENT)
    }

    val dashedSet = LineDataSet(entries, "Gaps").apply {
        lineWidth = 2f
        setDrawValues(false)
        setDrawCircles(false)
        colors = dashedColors.reversed()
        enableDashedLine(10f, 10f, 0f) // Only this set is dashed
    }

    return Pair(solidSet, dashedSet)
}

internal fun createLineChartView(
    isDarkTheme: Boolean,
    context: Context,
    points: List<Float>
): LineChart {
    return LineChart(context).apply {
        // General Setup
        description.isEnabled = false
        setTouchEnabled(false)
        setDrawGridBackground(false)
        isDragEnabled = false
        setScaleEnabled(false)
        setPinchZoom(false)

        // Theme Styling
        val contentColor = if (isDarkTheme) Color.WHITE else Color.BLACK
        setBackgroundColor(if (isDarkTheme) Color.TRANSPARENT else backgroundColor)

        xAxis.apply {
            enableGridDashedLine(10f, 10f, 0f)
            axisMinimum = -X_AXIS_ELEMENTS_COUNT
            axisMaximum = 0f
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            setDrawGridLines(false)
            gridColor = contentColor
        }
        axisLeft.apply {
            enableGridDashedLine(10f, 10f, 0f)
            gridColor = contentColor
            textColor = contentColor
        }
        axisRight.isEnabled = false

        // Custom Legend
        // We manually define one entry so the user doesn't see "Gaps" in the legend.
        legend.apply {
            isEnabled = true
            textColor = contentColor // Matches theme text color
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            xEntrySpace = 20f // Add some space between the two legend items

            val validEntry = LegendEntry().apply {
                label = "Recent Measurements"
                form = Legend.LegendForm.LINE
                formColor = customBlue
                formLineWidth = 2f
                formSize = 15f
            }
            // Adding two small segment lines to have the effect of dashed lines.
            val dashPart1 = LegendEntry().apply {
                label = null // Leave label null so it sits next to the next part
                form = Legend.LegendForm.LINE
                formColor = signalLostColor
                formLineWidth = 2f
                formSize = 4f // Small segment
            }

            val dashPart2 = LegendEntry().apply {
                label = "No Signal"
                form = Legend.LegendForm.LINE
                formColor = signalLostColor
                formLineWidth = 2f
                formSize = 4f // Small segment
            }

            setCustom(listOf(validEntry, dashPart1, dashPart2))
        }

        // Data Processing & Binding
        val (solidSet, dashedSet) = prepareTwoDataSets(points)

        // Add both sets to LineData.
        // Make sure to the order: the last one added is drawn on top.
        data = LineData(dashedSet, solidSet)
    }
}

private fun updateData(points: List<Float>, chart: LineChart) {
    val (newSolid, newDashed) = prepareTwoDataSets(points)

    chart.data?.let { lineData ->
        // We assume index 0 is dashed and index 1 is solid based on creation order
        val dashedSet = lineData.getDataSetByIndex(0) as? LineDataSet
        val solidSet = lineData.getDataSetByIndex(1) as? LineDataSet

        dashedSet?.apply {
            values = newDashed.values
            colors = newDashed.colors
        }
        solidSet?.apply {
            values = newSolid.values
            colors = newSolid.colors
        }

        lineData.notifyDataChanged()
        chart.notifyDataSetChanged()
        chart.invalidate()
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
