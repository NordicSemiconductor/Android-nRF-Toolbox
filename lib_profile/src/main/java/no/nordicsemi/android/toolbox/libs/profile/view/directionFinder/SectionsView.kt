package no.nordicsemi.android.toolbox.libs.profile.view.directionFinder

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.core.data.DFSServiceData
import no.nordicsemi.android.toolbox.libs.core.data.SensorData
import no.nordicsemi.android.toolbox.libs.core.data.medianValue
import no.nordicsemi.android.toolbox.libs.core.data.selectedMeasurementSectionValues
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun DistanceControlSection(
    data: DFSServiceData,
    onEvent: (DeviceConnectionViewEvent) -> Unit
) {
    ScreenSection {
        SectionTitle(
            resId = R.drawable.ic_control,
            title = stringResource(id = R.string.control_panel),
        )

        Spacer(modifier = Modifier.padding(8.dp))

        ControlView(data, onEvent)
    }
}

@Composable
internal fun AzimuthSection(data: DFSServiceData) {
    ScreenSection {
        SectionTitle(
            resId = R.drawable.ic_azimuth, stringResource(id = R.string.azimuth_section)
        )

        Spacer(modifier = Modifier.padding(8.dp))

        Box {
            Image(
                painter = painterResource(id = R.drawable.ic_azimuth),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface),
                contentDescription = null,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape
                    )
                    .height(200.dp)
                    .width(200.dp)
            )
            // TODO: Verify the param supplied to AzimuthView.
            data.data.values.forEach {
                when {
                    it.azimuth != null -> AzimuthView(it)
                }
            }
        }
    }
}

@Composable
internal fun ElevationSection(data: DFSServiceData) {
    ScreenSection {
        SectionTitle(
            resId = R.drawable.ic_elevation, stringResource(id = R.string.elevation_section)
        )

        Spacer(modifier = Modifier.padding(8.dp))

        Row(modifier = Modifier.padding(end = 50.dp)) {
            // TODO: Verify the param supplied to ElevationView.
            data.data.values.forEach { sensorData ->
                when {
                    sensorData.elevation != null -> sensorData.elevation.medianValue { it.elevation }
                        ?.let { it1 -> ElevationView(it1) }
                }
            }
        }
    }
}

@Composable
internal fun MeasuresSection(data: SensorData) {
    ScreenSection {
        MeasuresView(data = data)
    }
}

@Composable
internal fun SettingSection(serviceData: SensorData, onEvent: (DeviceConnectionViewEvent) -> Unit) {
    ScreenSection {
        SettingsView(data = serviceData, onEvent)
    }
}

@Composable
internal fun LinearDataSection(viewEntity: SensorData) {
    ScreenSection {
        SectionTitle(R.drawable.ic_distance, stringResource(id = R.string.distance_section))

        Spacer(modifier = Modifier.padding(8.dp))

        LinearDataView(viewEntity)
    }
}

@Composable
internal fun DataSmoothingViewSection(data: SensorData) {
    ScreenSection {
        SectionTitle(
            R.drawable.ic_distance,
            stringResource(id = R.string.measurement_details_section)
        )

        Spacer(modifier = Modifier.padding(8.dp))

        data.selectedMeasurementSectionValues()?.let { DataSmoothingView(it) }

    }
}
