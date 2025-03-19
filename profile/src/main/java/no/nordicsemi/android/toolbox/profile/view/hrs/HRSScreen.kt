package no.nordicsemi.android.toolbox.profile.view.hrs

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
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
import no.nordicsemi.android.toolbox.profile.data.HRSServiceData
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.HRSViewEvent.SwitchZoomEvent
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun HRSScreen(
    hrsServiceData: HRSServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ScreenSection {
            SectionTitle(
                icon = Icons.Default.MonitorHeart,
                title = stringResource(id = R.string.hrs_section_data),
                menu = {
                    MagnifyingGlass(hrsServiceData.zoomIn) { onClickEvent(it) }
                }
            )

            LineChartView(hrsServiceData, hrsServiceData.zoomIn)
            hrsServiceData.heartRate?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = it.displayHeartRate(),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

}

@Composable
private fun MagnifyingGlass(zoomIn: Boolean, onEvent: (DeviceConnectionViewEvent) -> Unit) {
    val icon = when (zoomIn) {
        true -> R.drawable.ic_zoom_out
        false -> R.drawable.ic_zoom_in
    }
    Icon(
        painter = painterResource(id = icon),
        contentDescription = stringResource(id = R.string.hrs_zoom_icon),
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onEvent(SwitchZoomEvent) }
            .padding(8.dp)
    )
}

@Preview
@Composable
private fun HRSScreenPreview() {
    HRSScreen(
        hrsServiceData = HRSServiceData(
            data = listOf(),
            bodySensorLocation = 0,
            zoomIn = false
        ),
        onClickEvent = {}
    )
}

/** The number of elements to display on the X axis. */
private const val X_AXIS_ELEMENTS_COUNT = 40f

/** The minimum value for the Y axis. */
private const val AXIS_MIN = 0

/** The maximum value for the Y axis. */
private const val AXIS_MAX = 300

@Composable
private fun LineChartView(state: HRSServiceData, zoomIn: Boolean) {
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

private fun createLineChartView(
    isDarkTheme: Boolean,
    context: Context,
    points: List<Int>,
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
        }
        axisLeft.apply {
            enableGridDashedLine(10f, 10f, 0f)
            axisMaximum = points.getMax(zoomIn)
            axisMinimum = points.getMin(zoomIn)
        }
        axisRight.isEnabled = false

        val entries = points.mapIndexed { i, v ->
            Entry(i.toFloat(), v.toFloat())
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

private fun updateData(isDarkTheme: Boolean, points: List<Int>, chart: LineChart, zoomIn: Boolean) {
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
