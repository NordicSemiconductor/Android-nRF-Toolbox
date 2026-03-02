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
        factory = { context ->
            createLineChartView(isSystemInDarkTheme, context)
        },
        update = { chart ->
            updateData(items, chart)
        }
    )
}

/**
 * Processes raw data into Chart Entries and Segment Colors.
 * This ensures the logic is identical for both initial load and updates.
 * @return Pair<LineDataSet, LineDataSet>: one for solid segments, one for dashed bridges.
 */
private fun prepareTwoDataSets(points: List<Float>): Pair<LineDataSet, LineDataSet>? {
    if (points.isEmpty()) return null

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

    // Helper to build colors and guarantee size matches entries
    fun getColors(isSolid: Boolean): List<Int> {
        val colorList = mutableListOf<Int>()
        for (i in 0 until points.size - 1) {
            val isMatch = if (isSolid) (points[i] != 0.0f && points[i + 1] != 0.0f)
            else (points[i] == 0.0f || points[i + 1] == 0.0f)
            colorList.add(if (isMatch) (if (isSolid) customBlue else signalLostColor) else Color.TRANSPARENT)
        }

        while (colorList.size < entries.size) {
            colorList.add(Color.TRANSPARENT)
        }
        return colorList.reversed()
    }

    val solidSet = LineDataSet(entries, "Valid Data").apply {
        lineWidth = 3f
        setDrawValues(false)
        setDrawCircles(false)
        colors = getColors(true)
    }

    val dashedSet = LineDataSet(entries, "Gaps").apply {
        lineWidth = 2f
        setDrawValues(false)
        setDrawCircles(false)
        colors = getColors(false)
        enableDashedLine(10f, 10f, 0f)
    }

    return Pair(solidSet, dashedSet)
}

private fun createLineChartView(
    isDarkTheme: Boolean,
    context: Context,
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
    }
}

private fun updateData(points: List<Float>, chart: LineChart) {
    val result = prepareTwoDataSets(points)

    if (result == null) {
        chart.data = null // Clears the chart when no data
    } else {
        val (newSolid, newDashed) = result
        chart.data = LineData(newDashed, newSolid)
    }

    chart.notifyDataSetChanged()
    chart.invalidate()
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
