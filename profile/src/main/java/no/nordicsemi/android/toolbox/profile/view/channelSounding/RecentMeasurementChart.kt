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
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
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

internal fun createLineChartView(
    isDarkTheme: Boolean,
    context: Context,
    points: List<Float>
): LineChart {
    return LineChart(context).apply {
        description.isEnabled = false

        legend.isEnabled = true

        setTouchEnabled(false)

        setDrawGridBackground(false)

        isDragEnabled = false
        setScaleEnabled(false)
        setPinchZoom(false)

        if (isDarkTheme) {
            setBackgroundColor(Color.TRANSPARENT)
            xAxis.gridColor = Color.WHITE
            xAxis.textColor = Color.WHITE
            axisLeft.gridColor = Color.WHITE
            axisLeft.textColor = Color.WHITE
        } else {
            setBackgroundColor(backgroundColor)
            xAxis.gridColor = Color.BLACK
            xAxis.textColor = Color.BLACK
            axisLeft.gridColor = Color.BLACK
            axisLeft.textColor = Color.BLACK
        }

        xAxis.apply {
            xAxis.enableGridDashedLine(10f, 10f, 0f)

            axisMinimum = -X_AXIS_ELEMENTS_COUNT
            axisMaximum = 0f
            setAvoidFirstLastClipping(true)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false) // Hide X-axis labels
            setDrawGridLines(false) // Hide vertical grid lines
        }
        axisLeft.apply {
            enableGridDashedLine(10f, 10f, 0f)
        }
        axisRight.isEnabled = false

        val entries = points.mapIndexedNotNull { i, v ->
            if (v == 0.0f) null else Entry(-i.toFloat(), v)
        }.reversed()

        legend.apply {
            isEnabled = true
            textColor = customBlue
            form = Legend.LegendForm.LINE
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        }

        // create a dataset and give it a type
        if (data != null && data.dataSetCount > 0) {
            val set1 = data!!.getDataSetByIndex(0) as LineDataSet
            set1.values = entries
            set1.notifyDataSetChanged()
            data!!.notifyDataChanged()
            notifyDataSetChanged()
        } else {
            val set1 = LineDataSet(entries, "Recent Measurements")

            set1.setDrawIcons(false)
            set1.setDrawValues(false)

            // draw dashed line
            set1.enableDashedLine(0f, 0f, 0f)

            // blue lines and points
            set1.color = customBlue
            set1.setDrawCircles(false)

            // line thickness and point size
            set1.lineWidth = 3f
//            set1.circleRadius = 3f

            // draw points as solid circles
            set1.setDrawCircleHole(false)

            // customize legend entry
            set1.formLineWidth = 1f
            set1.formLineWidth = 2f
            set1.formSize = 15f

            // text size of values
            set1.valueTextSize = 9f

            // draw selection line as dashed
//            set1.enableDashedHighlightLine(10f, 5f, 0f)

            val dataSets = ArrayList<ILineDataSet>()
            dataSets.add(set1) // add the data sets

            // create a data object with the data sets
            val data = LineData(dataSets)

            // set data
            setData(data)
        }
    }
}

private fun updateData(points: List<Float>, chart: LineChart) {
    // We map to entries ONLY if the value is not 0.0f
    // and use -i.toFloat() to maintain the reverse-chronological X-axis logic.
    val entries = points.mapIndexedNotNull { i, v ->
        if (v == 0.0f) null else Entry(-i.toFloat(), v)
    }.reversed()

    with(chart) {
        if (data != null && data.dataSetCount > 0) {
            val set1 = data!!.getDataSetByIndex(0) as LineDataSet
            set1.values = entries

            // This tells the chart NOT to draw a line between the gaps
            // (Standard behavior for missing entries, but good to be explicit)
            set1.isVisible = true

            set1.notifyDataSetChanged()
            data!!.notifyDataChanged()
            notifyDataSetChanged()
            invalidate()
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
                3.0f
            )
        )
    }
}
