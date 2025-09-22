package no.nordicsemi.android.toolbox.profile.view.channelSounding

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
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

private const val X_AXIS_ELEMENTS_COUNT = 40.0f

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
@Composable
internal fun LineChartView(previousData: List<Float>) {
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

        setTouchEnabled(true)

        setDrawGridBackground(false)

        isDragEnabled = true
        setScaleEnabled(false)
        setPinchZoom(true)

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
            xAxis.enableGridDashedLine(10f, 10f, 0f)

            axisMinimum = -X_AXIS_ELEMENTS_COUNT
            axisMaximum = 0f
            setAvoidFirstLastClipping(false)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false) // Hide X-axis labels
        }
        axisLeft.apply {
            enableGridDashedLine(10f, 10f, 0f)
        }
        axisRight.isEnabled = false

        val entries = points.mapIndexed { i, v ->
            Entry(-i.toFloat(), v)
        }.reversed()

        legend.apply {
            isEnabled = true
            textColor = if (isDarkTheme) Color.WHITE else Color.BLUE
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
            val set1 = LineDataSet(entries, "Previous Measurements")

            set1.setDrawIcons(false)
            set1.setDrawValues(false)

            // draw dashed line
            set1.enableDashedLine(0f, 0f, 0f)

            // blue lines and points
            set1.color = Color.BLUE
            set1.setDrawCircles(false)

            // line thickness and point size
            set1.lineWidth = 2f
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
    val entries = points.mapIndexed { i, v ->
        Entry(-i.toFloat(), v)
    }.reversed()

    with(chart) {

        if (data != null && data.dataSetCount > 0) {
            val set1 = data!!.getDataSetByIndex(0) as LineDataSet
            set1.values = entries
            set1.notifyDataSetChanged()
            data!!.notifyDataChanged()
            notifyDataSetChanged()
            invalidate()
        }
    }
}
