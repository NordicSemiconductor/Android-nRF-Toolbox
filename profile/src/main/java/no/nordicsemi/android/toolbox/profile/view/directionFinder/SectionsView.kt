package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.DFSServiceData
import no.nordicsemi.android.toolbox.profile.data.SensorData
import no.nordicsemi.android.toolbox.profile.data.directionFinder.Range
import no.nordicsemi.android.toolbox.profile.data.directionFinder.distanceValue
import no.nordicsemi.android.toolbox.profile.data.directionFinder.elevationValue
import no.nordicsemi.android.toolbox.profile.data.directionFinder.medianValue
import no.nordicsemi.android.toolbox.profile.data.directionFinder.selectedMeasurementSectionValues
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.ui.view.ReversedSectionTitle
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun DistanceSection(
    data: SensorData,
    range: Range
) {
    ScreenSection {
        SectionTitle(R.drawable.ic_distance, stringResource(id = R.string.distance_section))
        data.distanceValue()?.let { DistanceView(value = it, range = range) }
    }
}

@Preview(showBackground = true)
@Composable
private fun DistanceSectionPreview() {
    DistanceSection(SensorData(), Range(0, 50))
}

@Composable
internal fun DistanceControlSection(
    data: DFSServiceData,
    sensorData: SensorData,
    onEvent: (DeviceConnectionViewEvent) -> Unit
) {
    ScreenSection {
        SectionTitle(
            resId = R.drawable.ic_control,
            title = stringResource(id = R.string.control_panel),
        )
        ControlView(data, sensorData, onEvent)
    }
}

@Preview(showBackground = true)
@Composable
private fun DistanceControlSectionPreview() {
    DistanceControlSection(DFSServiceData(), SensorData()) {}
}

@Composable
internal fun AzimuthSection(data: SensorData, distanceRange: Range) {
    ScreenSection {
        SectionTitle(
            resId = R.drawable.ic_azimuth, stringResource(id = R.string.azimuth_section)
        )
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
            AzimuthView(data, distanceRange)
        }
    }
}

@Preview
@Composable
private fun AzimuthSectionPreview() {
    AzimuthSection(SensorData(), Range(0, 50))
}

@Composable
internal fun ElevationSection(data: SensorData) {
    ScreenSection {
        SectionTitle(
            resId = R.drawable.ic_elevation, stringResource(id = R.string.elevation_section)
        )

        Row(modifier = Modifier.padding(end = 50.dp)) {
            // TODO: Verify the param supplied to ElevationView.
            data.elevation.medianValue { it.elevation }?.let { ElevationView(it) }
        }
    }
}

@Preview
@Composable
private fun ElevationSectionPreview() {
    ElevationSection(SensorData())
}

@Composable
internal fun MeasuresSection(data: SensorData) {
    ScreenSection {
        MeasuresView(data = data)
    }
}

@Composable
internal fun SettingSection(
    serviceData: SensorData,
    range: Range,
    onEvent: (DeviceConnectionViewEvent) -> Unit
) {
    ScreenSection {
        SettingsView(data = serviceData, range, onEvent)
    }
}

@Preview
@Composable
private fun SettingSectionPreview() {
    SettingSection(SensorData(), Range(0, 50)) { }
}

@Composable
internal fun LinearDataSection(data: SensorData, distanceRange: Range) {
    ScreenSection {
        SectionTitle(R.drawable.ic_distance, stringResource(id = R.string.distance_section))
        LinearDataView(data, distanceRange)
    }
}

@Preview
@Composable
private fun LinearDataSectionPreview() {
    LinearDataSection(SensorData(), Range(0, 50))
}

@Composable
internal fun DataSmoothingViewSection(data: SensorData) {
    ScreenSection {
        SectionTitle(
            R.drawable.ic_distance,
            stringResource(id = R.string.measurement_details_section)
        )
        data.selectedMeasurementSectionValues()?.let { DataSmoothingView(it) }

    }
}

@Preview
@Composable
private fun DataSmoothingViewSectionPreview() {
    DataSmoothingViewSection(SensorData())
}

@Composable
internal fun AzimuthAndElevationSection(data: SensorData, range: Range) {
    ScreenSection {
        BoxWithConstraints {
            SectionTitle(
                R.drawable.ic_azimuth,
                stringResource(id = R.string.azimuth_section)
            )

            val elevationTitle = if (maxWidth < 300.dp) {
                stringResource(id = R.string.elevation_section).substring(0, 4)
            } else {
                stringResource(id = R.string.elevation_section)
            }

            ReversedSectionTitle(
                R.drawable.ic_elevation,
                elevationTitle
            )
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                ElevationView(value = data.elevationValue()!!)
            }

            Box(modifier = Modifier.align(Alignment.TopStart)) {
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
                AzimuthView(data, range)
            }
        }
    }
}
