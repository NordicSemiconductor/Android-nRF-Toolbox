package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.lib.profile.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.lib.profile.directionFinder.azimuthal.AzimuthMeasurementData
import no.nordicsemi.android.lib.profile.directionFinder.elevation.ElevationMeasurementData
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.DFSServiceData
import no.nordicsemi.android.toolbox.profile.data.SensorData
import no.nordicsemi.android.toolbox.profile.data.SensorValue
import no.nordicsemi.android.toolbox.profile.data.directionFinder.Range
import no.nordicsemi.android.toolbox.profile.data.directionFinder.availableSections
import no.nordicsemi.android.toolbox.profile.data.directionFinder.displayAzimuth
import no.nordicsemi.android.toolbox.profile.data.directionFinder.elevationValue
import no.nordicsemi.android.toolbox.profile.data.directionFinder.medianValue
import no.nordicsemi.android.toolbox.profile.data.directionFinder.selectedMeasurementSectionValues
import no.nordicsemi.android.toolbox.profile.viewmodel.DFSEvent
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun DistanceSection(
    distanceValue: Int,
    range: Range,
    onClick: (DFSEvent) -> Unit,
) {
    ScreenSection {
        SectionTitle(R.drawable.ic_distance, stringResource(id = R.string.distance_section))
        DistanceView(value = distanceValue, range = range)

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$distanceValue dm",
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Column {
            Text(
                stringResource(R.string.distance_range),
                style = MaterialTheme.typography.titleSmall
            )
            RangeSlider(range) {
                onClick(DFSEvent.OnRangeChangedEvent(it))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DistanceSectionPreview() {
    DistanceSection(15, Range(0, 50)) {}
}

@Composable
internal fun AzimuthSection(data: SensorData, distanceRange: Range) {
    ScreenSection {
        SectionTitle(
            resId = R.drawable.ic_azimuth, stringResource(id = R.string.azimuth_section)
        )
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
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
            AzimuthView(data, distanceRange)
        }

        data.displayAzimuth()?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}

@Preview
@Composable
private fun AzimuthSectionPreview() {
    val sensorData = SensorData(
        azimuth = SensorValue(
            values = listOf(
                AzimuthMeasurementData(
                    azimuth = 20,
                    address = PeripheralBluetoothAddress.TEST
                )
            )
        )
    )
    AzimuthSection(sensorData, Range(0, 50))
}

@Composable
internal fun ElevationSection(data: SensorData) {
    ScreenSection {
        SectionTitle(
            resId = R.drawable.ic_elevation, stringResource(id = R.string.elevation_section)
        )

        Row(
            modifier = Modifier.padding(end = 50.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            data.elevation.medianValue { it.elevation }?.let { ElevationView(it, data) }
        }
    }
}

@Preview
@Composable
private fun ElevationSectionPreview() {
    val sensorData = SensorData(
        elevation = SensorValue(
            values = listOf(
                ElevationMeasurementData(
                    address = PeripheralBluetoothAddress.TEST,
                    elevation = 30
                )
            )
        )
    )
    ElevationSection(sensorData)
}

@Composable
internal fun SettingSection(
    serviceData: DFSServiceData,
    data: SensorData
) {
    ScreenSection {
        Text(
            text = stringResource(R.string.distance_settings),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
        LinearDataView(data, serviceData.distanceRange)
    }
}

@Preview
@Composable
private fun SettingSectionPreview() {
    SettingSection(DFSServiceData(), SensorData())
}

@Composable
internal fun LinearDataSection(
    serviceData: DFSServiceData,
    data: SensorData,
    distanceRange: Range,
    onClick: (DFSEvent) -> Unit
) {
    ScreenSection {
        ControlView(serviceData, data, onClick)
        LinearDataView(data, distanceRange)
    }
}

@Preview
@Composable
private fun LinearDataSectionPreview() {
    LinearDataSection(DFSServiceData(), SensorData(), Range(0, 50)) {}
}

@Composable
internal fun DataSmoothingViewSection(data: SensorData, onClick: (DFSEvent) -> Unit) {
    if (data.availableSections().isNotEmpty()) {
        MeasurementDetailModeView(data, onClick)
    }
    data.selectedMeasurementSectionValues()?.let { DataSmoothingView(it) }
}

@Preview
@Composable
private fun DataSmoothingViewSectionPreview() {
    DataSmoothingViewSection(SensorData()) {}
}

@Composable
internal fun AzimuthAndElevationSection(data: SensorData, range: Range) {
    ScreenSection {
        SectionTitle(
            R.drawable.ic_azimuth,
            stringResource(id = R.string.azimuth_section)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
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
                            .size(200.dp)
                    )
                    AzimuthView(data, range)
                }
                data.displayAzimuth()?.let {
                    Text(
                        text = "Direction relative to North: $it",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }

            SectionTitle(
                R.drawable.ic_elevation,
                stringResource(id = R.string.elevation_section)
            )
            Box {
                ElevationView(value = data.elevationValue()!!, data)
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AzimuthAndElevationSectionPreview() {
    val sensorData = SensorData(
        azimuth = SensorValue(
            values = listOf(
                AzimuthMeasurementData(
                    azimuth = 50,
                    address = PeripheralBluetoothAddress.TEST
                )
            )
        ),
        elevation = SensorValue(
            values = listOf(
                ElevationMeasurementData(
                    address = PeripheralBluetoothAddress.TEST,
                    elevation = 30
                )
            )
        )
    )
    AzimuthAndElevationSection(sensorData, Range(0, 50))
}
