package no.nordicsemi.android.hrs.view

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.Utils
import no.nordicsemi.android.hrs.R
import no.nordicsemi.android.hrs.viewmodel.HRSViewState
import no.nordicsemi.android.theme.NordicColors
import no.nordicsemi.android.theme.view.BatteryLevelView
import java.util.*

@Composable
internal fun ContentView(state: HRSViewState, onEvent: (HRSScreenViewEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            backgroundColor = NordicColors.NordicGray4.value(),
            shape = RoundedCornerShape(10.dp),
            elevation = 0.dp
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                LineChartView(state)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BatteryLevelView(state.batteryLevel)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
            onClick = { onEvent(DisconnectEvent) }
        ) {
            Text(text = stringResource(id = R.string.disconnect))
        }
    }
}

@Composable
internal fun LineChartView(state: HRSViewState) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { createLineChartView(it, state) },
        update = { updateData(state.points, it)  }
    )
}

internal fun createLineChartView(context: Context, state: HRSViewState): LineChart {
    return LineChart(context).apply {
        setBackgroundColor(Color.WHITE)

        description.isEnabled = false

        setTouchEnabled(true)

//        setOnChartValueSelectedListener(this)
        setDrawGridBackground(false)

        isDragEnabled = true
        setScaleEnabled(true)
        setPinchZoom(true)

        xAxis.apply {
            xAxis.enableGridDashedLine(10f, 10f, 0f)
        }
        axisLeft.apply {
            enableGridDashedLine(10f, 10f, 0f)

            axisMaximum = 300f
            axisMinimum = 100f
        }
        axisRight.isEnabled = false

        //---

        val entries = state.points.mapIndexed { i, v ->
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

            // draw dashed line

            // draw dashed line
            set1.enableDashedLine(10f, 5f, 0f)

            // black lines and points

            // black lines and points
            set1.color = Color.BLACK
            set1.setCircleColor(Color.BLACK)

            // line thickness and point size

            // line thickness and point size
            set1.lineWidth = 1f
            set1.circleRadius = 3f

            // draw points as solid circles

            // draw points as solid circles
            set1.setDrawCircleHole(false)

            // customize legend entry

            // customize legend entry
            set1.formLineWidth = 1f
            set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
            set1.formSize = 15f

            // text size of values

            // text size of values
            set1.valueTextSize = 9f

            // draw selection line as dashed

            // draw selection line as dashed
            set1.enableDashedHighlightLine(10f, 5f, 0f)

            // set the filled area

            // set the filled area
            set1.setDrawFilled(true)
            set1.fillFormatter = IFillFormatter { _, _ ->
                axisLeft.axisMinimum
            }

            // set color of filled area

            // set color of filled area
            if (Utils.getSDKInt() >= 18) {
                // drawables only supported on api level 18 and above
                val drawable = ContextCompat.getDrawable(context, R.drawable.fade_red)
                set1.fillDrawable = drawable
            } else {
                set1.fillColor = Color.BLACK
            }

            val dataSets = ArrayList<ILineDataSet>()
            dataSets.add(set1) // add the data sets


            // create a data object with the data sets

            // create a data object with the data sets
            val data = LineData(dataSets)

            // set data

            // set data
            setData(data)
        }
    }
}

private fun updateData(points: List<Int>, chart: LineChart) {
    val entries = points.mapIndexed { i, v ->
        Entry(i.toFloat(), v.toFloat())
    }

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

@Preview
@Composable
private fun Preview() {
    ContentView(state = HRSViewState()) { }
}
