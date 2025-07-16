package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.lib.profile.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.lib.profile.directionFinder.elevation.ElevationMeasurementData
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.SensorData
import no.nordicsemi.android.toolbox.profile.data.SensorValue
import no.nordicsemi.android.toolbox.profile.data.directionFinder.medianValue
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

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
