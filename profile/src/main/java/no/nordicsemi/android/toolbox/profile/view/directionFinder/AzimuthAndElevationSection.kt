package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.parser.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.toolbox.profile.parser.directionFinder.azimuthal.AzimuthMeasurementData
import no.nordicsemi.android.toolbox.profile.parser.directionFinder.elevation.ElevationMeasurementData
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.SensorData
import no.nordicsemi.android.toolbox.profile.data.SensorValue
import no.nordicsemi.android.toolbox.profile.data.directionFinder.Range
import no.nordicsemi.android.toolbox.profile.data.directionFinder.displayAzimuth
import no.nordicsemi.android.toolbox.profile.data.directionFinder.elevationValue
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

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