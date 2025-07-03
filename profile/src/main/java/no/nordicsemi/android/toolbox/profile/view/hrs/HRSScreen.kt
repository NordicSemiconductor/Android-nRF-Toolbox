package no.nordicsemi.android.toolbox.profile.view.hrs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.HRSServiceData
import no.nordicsemi.android.toolbox.profile.viewmodel.HRSEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.HRSEvent.SwitchZoomEvent
import no.nordicsemi.android.ui.view.KeyValueColumn
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionRow
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun HRSScreen(
    hrsServiceData: HRSServiceData,
    onClickEvent: (HRSEvent) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ScreenSection(modifier = Modifier.padding(0.dp) /* No padding */) {
            Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
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
                            .padding(16.dp)
                    ) {
                        Text(
                            text = hrsServiceData.displayHeartRate(),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            HorizontalDivider()

            hrsServiceData.bodySensorLocation?.let {
                SectionRow {
                    KeyValueColumn(
                        stringResource(id = R.string.body_sensor_location),
                        hrsServiceData.displayBodySensorLocation(),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }

}

@Composable
private fun MagnifyingGlass(zoomIn: Boolean, onEvent: (HRSEvent) -> Unit) {
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

@Composable
private fun LineChartView(state: HRSServiceData, zoomIn: Boolean) {
    val items = state.heartRates.takeLast(state.heartRates.size)
    val isSystemInDarkTheme = isSystemInDarkTheme()


    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start

    ) {
        Text(
            text = "Heart Rate (bpm)",
            modifier = Modifier.rotate(-90f),
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                factory = { createLineChartView(isSystemInDarkTheme, it, items, zoomIn) },
                update = { updateData(isSystemInDarkTheme, items, it, zoomIn) }
            )
            Text(
                text = "Index",
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }

}

@Composable
private fun ChartLabel(
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(8.dp)
                .clip(RectangleShape)
                .background(Color.Red)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

