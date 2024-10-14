package no.nordicsemi.android.toolbox.libs.profile.view

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.profile.service.HRSServiceData
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.SwitchZoomEvent
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
                resId = R.drawable.ic_chart_line,
                title = stringResource(id = R.string.hrs_section_data),
                menu = { Menu(hrsServiceData.zoomIn) { onClickEvent(it) } }
            )

            LineChartView(hrsServiceData, hrsServiceData.zoomIn)
        }
    }

}

@Composable
private fun Menu(zoomIn: Boolean, onEvent: (DeviceConnectionViewEvent) -> Unit) {
    val icon = when (zoomIn) {
        true -> R.drawable.ic_zoom_out
        false -> R.drawable.ic_zoom_in
    }
    IconButton(onClick = { onEvent(SwitchZoomEvent) }) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = stringResource(id = R.string.hrs_zoom_icon)
        )
    }
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
    val items = state.heartRates.takeLast(X_AXIS_ELEMENTS_COUNT.toInt()).reversed()
    val isSystemInDarkTheme = isSystemInDarkTheme()
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { createLineChartView(isSystemInDarkTheme, it, items, zoomIn) },
        update = { updateData(items, it, zoomIn) }
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

        setTouchEnabled(false)

        setDrawGridBackground(false)

        isDragEnabled = true
        setScaleEnabled(false)
        setPinchZoom(false)

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
            Entry(-i.toFloat(), v.toFloat())
        }.reversed()

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

            // draw dashed line
            set1.enableDashedLine(10f, 5f, 0f)

            // black lines and points
            if (isDarkTheme) {
                set1.color = Color.WHITE
                set1.setCircleColor(Color.WHITE)
            } else {
                set1.color = Color.BLACK
                set1.setCircleColor(Color.BLACK)
            }

            // line thickness and point size
            set1.lineWidth = 1f
            set1.circleRadius = 3f

            // draw points as solid circles
            set1.setDrawCircleHole(false)

            // customize legend entry
            set1.formLineWidth = 1f
            set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
            set1.formSize = 15f

            // text size of values
            set1.valueTextSize = 9f

            // draw selection line as dashed
            set1.enableDashedHighlightLine(10f, 5f, 0f)

            val dataSets = ArrayList<ILineDataSet>()
            dataSets.add(set1) // add the data sets

            // create a data object with the data sets
            val data = LineData(dataSets)

            // set data
            setData(data)
        }
    }
}

private fun updateData(points: List<Int>, chart: LineChart, zoomIn: Boolean) {
    val entries = points.mapIndexed { i, v ->
        Entry(-i.toFloat(), v.toFloat())
    }.reversed()

    with(chart) {
        axisLeft.apply {
            axisMaximum = points.getMax(zoomIn)
            axisMinimum = points.getMin(zoomIn)
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
