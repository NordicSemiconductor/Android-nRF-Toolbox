package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.lib.profile.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.lib.profile.directionFinder.azimuthal.AzimuthMeasurementData
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.SensorData
import no.nordicsemi.android.toolbox.profile.data.SensorValue
import no.nordicsemi.android.toolbox.profile.data.directionFinder.Range
import no.nordicsemi.android.toolbox.profile.data.directionFinder.displayAzimuth
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

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
