package no.nordicsemi.android.toolbox.profile.view.hrs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.viewmodel.HRSEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.HRSEvent.SwitchZoomEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.HRSViewModel
import no.nordicsemi.android.ui.view.KeyValueColumn
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionRow
import no.nordicsemi.android.ui.view.SectionTitle
import no.nordicsemi.android.ui.view.animate.AnimatedHeart

@Composable
internal fun HRSScreen() {
    val hrsViewModel = hiltViewModel<HRSViewModel>()
    val hrsServiceData by hrsViewModel.hrsState.collectAsStateWithLifecycle()
    val onClickEvent: (HRSEvent) -> Unit = { hrsViewModel.onEvent(it) }

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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AnimatedHeart(modifier = Modifier.padding(8.dp))
                        Text(
                            text = hrsServiceData.displayHeartRate(),
                            style = MaterialTheme.typography.titleLarge,
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
