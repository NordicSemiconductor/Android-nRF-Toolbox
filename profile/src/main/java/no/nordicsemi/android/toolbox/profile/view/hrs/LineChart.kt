package no.nordicsemi.android.toolbox.profile.view.hrs

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import no.nordicsemi.android.toolbox.profile.data.HRSServiceData

@Composable
internal fun LineChartView(state: HRSServiceData, zoomIn: Boolean) {
    val items = state.heartRates.takeLast(state.heartRates.size)
    val isSystemInDarkTheme = isSystemInDarkTheme()

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { createLineChartView(isSystemInDarkTheme, it, items, zoomIn) },
        update = { updateData(isSystemInDarkTheme, items, it, zoomIn) }
    )
}

/** The number of elements to display on the X axis. */
private const val X_AXIS_ELEMENTS_COUNT = 40f

/** The minimum value for the Y axis. */
private const val AXIS_MIN = 0

/** The maximum value for the Y axis. */
private const val AXIS_MAX = 300

internal fun createLineChartView(
    isDarkTheme: Boolean,
    context: Context,
    points: List<Int>,
    zoomIn: Boolean
): LineChart {
    return LineChart(context).apply {
        description.isEnabled = false
        legend.isEnabled = true
        setTouchEnabled(true) // Enable touch gestures
        setDrawGridBackground(false)
        isDragEnabled = true
        setScaleEnabled(false) // Enable scaling
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
            setAvoidFirstLastClipping(false)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false) // Hide X-axis labels
            setDrawGridLines(false) // Hide vertical grid lines
        }
        axisLeft.apply {
            enableGridDashedLine(10f, 10f, 0f)
            axisMaximum = points.getMax(zoomIn)
            axisMinimum = points.getMin(zoomIn)
//            setDrawGridLines(true) // Show horizontal grid lines
        }
        axisRight.isEnabled = false

        val entries = points.mapIndexed { i, v ->
            Entry(i.toFloat(), v.toFloat())
        }

        legend.apply {
            isEnabled = true
            textColor = if (isDarkTheme) Color.WHITE else Color.RED
            form = Legend.LegendForm.LINE
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
        }
        // create a dataset and give it a type
        if (data != null && data.dataSetCount > 0) {
            val set1 = data!!.getDataSetByIndex(0) as LineDataSet
            set1.values = entries
            set1.notifyDataSetChanged()
            data!!.notifyDataChanged()
            notifyDataSetChanged()
        } else {
            val set1 = LineDataSet(entries, "Heart Rate")

            set1.setDrawIcons(false)
            set1.setDrawValues(false)

            // solid line
            set1.enableDashedLine(0f, 0f, 0f)

            // red line and points
            set1.color = Color.RED
            set1.setDrawCircles(false)

            // line thickness and point size
            set1.lineWidth = 2f

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

internal fun updateData(
    isDarkTheme: Boolean,
    points: List<Int>,
    chart: LineChart,
    zoomIn: Boolean
) {
    val entries = points.mapIndexed { i, v ->
        Entry(i.toFloat(), v.toFloat())
    }

    with(chart) {
        axisLeft.apply {
            axisMaximum = points.getMax(zoomIn)
            axisMinimum = points.getMin(zoomIn)
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

private fun List<Int>.getMin(zoomIn: Boolean): Float {
    return if (zoomIn) {
        minOrNull() ?: AXIS_MIN
    } else {
        AXIS_MIN
    }.toFloat()
}

private fun List<Int>.getMax(zoomIn: Boolean): Float {
    return if (zoomIn) {
        maxOrNull() ?: AXIS_MAX
    } else {
        AXIS_MAX
    }.toFloat()
}
