package no.nordicsemi.android.toolbox.profile.view.directionFinder

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
import no.nordicsemi.android.toolbox.profile.R

@Composable
internal fun DataSmoothingView2(data: List<Int>) {
    Text(
        text = stringResource(id = R.string.value_smoothing_info),
        style = MaterialTheme.typography.bodyMedium
    )

    Spacer(modifier = Modifier.padding(8.dp))

    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = "${data.maxOrNull()}",
            style = MaterialTheme.typography.labelSmall
        )

        Spacer(modifier = Modifier.padding(2.dp))
        DataSmoothingLineChart(data = data)
        Spacer(modifier = Modifier.padding(2.dp))

        Text(
            text = "0",
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
internal fun DataSmoothingLineChart(
    modifier: Modifier = Modifier,
    data: List<Int>
) {
    val maxValue = data.maxOrNull() ?: 1
    val points = data.mapIndexed { index, value ->
        val x = index.toFloat() / (data.size - 1)
        val y = value.toFloat() / maxValue
        Offset(x, y)
    }

    Canvas(
        modifier = modifier
            .height(200.dp)
            .fillMaxWidth()
    ) {
        val path = Path().apply {
            moveTo(points.first().x * size.width, size.height - points.first().y * size.height)
            points.drop(1).forEach { point ->
                lineTo(point.x * size.width, size.height - point.y * size.height)
            }
        }

        drawPath(
            path = path,
            color = androidx.compose.ui.graphics.Color.Blue,
            style = Stroke(width = 4.dp.toPx())
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DataSmoothingView2Prevoew(){
    DataSmoothingView2(data = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
}

private fun calculateYOffset(
    higherTransactionRateValue: Int,
    currentTransactionRate: Int,
    canvasHeight: Float
): Float {
    val maxAndCurrentValueDifference = (higherTransactionRateValue - currentTransactionRate)
    val relativePercentageOfScreen = (canvasHeight / higherTransactionRateValue)
    return maxAndCurrentValueDifference * relativePercentageOfScreen
}

private fun calculateHeight(
    higherTransactionRateValue: Int,
    currentTransactionRate: Int,
    canvasHeight: Float
): Float {
    val relativePercentageOfScreen = (canvasHeight / higherTransactionRateValue)
    return currentTransactionRate * relativePercentageOfScreen
}

@Composable
internal fun DataSmoothingView(data: List<Int>) {
    Text(
        text = stringResource(id = R.string.value_smoothing_info),
        style = MaterialTheme.typography.bodyMedium
    )

    Spacer(modifier = Modifier.padding(8.dp))

    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = "${data.maxOrNull()}",
            style = MaterialTheme.typography.labelSmall
        )

        Spacer(modifier = Modifier.padding(28.dp))
        SmoothingLineChartView(data = data)
        Spacer(modifier = Modifier.padding(28.dp))

        Text(
            text = "0",
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
internal fun SmoothingLineChartView(data: List<Int>) {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val maxValue = data.maxOrNull() ?: 1
    val zoomIn = false // Adjust as needed

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        factory = { context ->
            createSmoothingLineChartView(
                isSystemInDarkTheme,
                context,
                data,
                zoomIn,
                maxValue
            )
        },
        update = { chart -> updateSmoothingData(data, chart, zoomIn, maxValue) }
    )
}

@Preview(showBackground = true)
@Composable
private fun DataSmoothingViewPreview() {
    DataSmoothingView(data = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
}

private fun createSmoothingLineChartView(
    isDarkTheme: Boolean,
    context: Context,
    points: List<Int>,
    zoomIn: Boolean,
    maxValue: Int
): LineChart {
    return LineChart(context).apply {
        description.isEnabled = false
        legend.isEnabled = false
        setTouchEnabled(true)
        setDrawGridBackground(false)
        isDragEnabled = true
        setScaleEnabled(true)
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
            enableGridDashedLine(10f, 10f, 0f)
            axisMinimum = -points.size.toFloat()
            axisMaximum = 0f
            setAvoidFirstLastClipping(true)
            position = XAxis.XAxisPosition.BOTTOM
        }

        axisLeft.apply {
            enableGridDashedLine(10f, 10f, 0f)
            axisMaximum = points.getMaxValue(zoomIn, maxValue)
            axisMinimum = points.getMinValue(zoomIn)
        }

        axisRight.isEnabled = false

        val entries = points.mapIndexed { i, v ->
            Entry(-i.toFloat(), v.toFloat())
        }.reversed()

        if (data != null && data.dataSetCount > 0) {
            val set1 = data!!.getDataSetByIndex(0) as LineDataSet
            set1.values = entries
            set1.notifyDataSetChanged()
            data!!.notifyDataChanged()
            notifyDataSetChanged()
        } else {
            val set1 = LineDataSet(entries, "Smoothed Data")
            set1.setDrawIcons(false)
            set1.setDrawValues(false)
            set1.enableDashedLine(10f, 5f, 0f)

            set1.color = Color.MAGENTA
            /*if (isDarkTheme) {
                set1.color = Color.WHITE
                set1.setCircleColor(Color.WHITE)
            } else {
                set1.color = Color.BLACK
                set1.setCircleColor(Color.BLACK)
            }*/

            set1.lineWidth = 1f
            set1.circleRadius = 3f
            set1.setDrawCircleHole(false)
            set1.formLineWidth = 1f
            set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
            set1.formSize = 15f
            set1.valueTextSize = 9f
            set1.enableDashedHighlightLine(10f, 5f, 0f)

            val dataSets = ArrayList<ILineDataSet>()
            dataSets.add(set1)

            val chartData = LineData(dataSets)
            setData(chartData)
        }
    }
}

private fun updateSmoothingData(
    points: List<Int>,
    chart: LineChart,
    zoomIn: Boolean,
    maxValue: Int
) {
    val entries = points.mapIndexed { i, v ->
        Entry(-i.toFloat(), v.toFloat())
    }.reversed()

    with(chart) {
        axisLeft.apply {
            axisMaximum = points.getMaxValue(zoomIn, maxValue)
            axisMinimum = points.getMinValue(zoomIn)
        }

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

private fun List<Int>.getMinValue(zoomIn: Boolean): Float {
    return if (zoomIn) {
        minOrNull() ?: 0
    } else {
        0
    }.toFloat()
}

private fun List<Int>.getMaxValue(zoomIn: Boolean, maxValue: Int): Float {
    return if (zoomIn) {
        maxOrNull() ?: maxValue
    } else {
        maxValue
    }.toFloat()
}
