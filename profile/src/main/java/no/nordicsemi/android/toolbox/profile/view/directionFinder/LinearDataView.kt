package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.DFSServiceData
import no.nordicsemi.android.toolbox.profile.data.SensorData
import no.nordicsemi.android.toolbox.profile.data.SensorValue
import no.nordicsemi.android.toolbox.profile.data.directionFinder.bestEffortValue
import no.nordicsemi.android.toolbox.profile.data.directionFinder.ifftValue
import no.nordicsemi.android.toolbox.profile.data.directionFinder.isMcpdSectionAvailable
import no.nordicsemi.android.toolbox.profile.data.directionFinder.phaseSlopeValue
import no.nordicsemi.android.toolbox.profile.data.directionFinder.rssiValue
import no.nordicsemi.android.toolbox.profile.data.directionFinder.rttValue
import no.nordicsemi.android.toolbox.profile.parser.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.toolbox.profile.parser.directionFinder.QualityIndicator
import no.nordicsemi.android.toolbox.profile.parser.directionFinder.azimuthal.AzimuthMeasurementData
import no.nordicsemi.android.toolbox.profile.parser.directionFinder.distance.MCPDEstimate
import no.nordicsemi.android.toolbox.profile.parser.directionFinder.distance.McpdMeasurementData
import no.nordicsemi.android.toolbox.profile.parser.directionFinder.distance.RTTEstimate
import no.nordicsemi.android.toolbox.profile.parser.directionFinder.distance.RttMeasurementData
import no.nordicsemi.android.toolbox.profile.parser.directionFinder.elevation.ElevationMeasurementData

@Composable
internal fun LinearDataView(
    data: SensorData,
    range: IntRange
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        data.rttValue()?.let {
            Text(stringResource(id = R.string.rtt),
                style = MaterialTheme.typography.titleSmall)

            LinearDataItemView(
                name = stringResource(id = R.string.rtt_label),
                range = range,
                item = it,
                showLegend = true
            )
        }

        if (data.isMcpdSectionAvailable()) {
            Text(
                stringResource(id = R.string.mcpd),
                style = MaterialTheme.typography.titleSmall
            )

            data.ifftValue()?.let {
                LinearDataItemView(name = stringResource(id = R.string.ifft_label), range, it)
            }

            data.phaseSlopeValue()?.let {
                LinearDataItemView(name = stringResource(id = R.string.phase_label), range, it)
            }

            data.rssiValue()?.let {
                LinearDataItemView(name = stringResource(id = R.string.rssi_label), range, it)
            }

            data.bestEffortValue()?.let {
                LinearDataItemView(name = stringResource(id = R.string.best_label), range, it, showLegend = true)
            }

            // Add legend if IFFT is present.
            data.ifftValue()?.let {
                Spacer(modifier = Modifier.height(8.dp))
                IfftFullForm()
            }
        }
    }
}

@Composable
private fun LinearDataItemView(name: String, range: IntRange, item: Int, showLegend: Boolean = false) {
    val labelWidth = 48.dp

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Column(
                modifier = Modifier.width(labelWidth),
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = stringResource(R.string.dm_value, item),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            DistanceChartView(value = item, range = range)
        }

        if (showLegend) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = labelWidth)
            ) {
                Text(
                    text = stringResource(R.string.dm_value, range.first),
                    style = MaterialTheme.typography.labelSmall
                )

                val diff = range.last - range.first
                val part = (diff / 4)
                if (part > 0) {
                    Text(
                        text = stringResource(R.string.dm_value, range.first + part),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = stringResource(R.string.dm_value, range.first + 2 * part),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Text(
                    text = stringResource(R.string.dm_value, range.last),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LinearDataViewPreview() {
    LinearDataView(
        data = SensorData(
            azimuth = SensorValue(
                values = listOf(
                    AzimuthMeasurementData(
                        quality = QualityIndicator.POOR,
                        address = PeripheralBluetoothAddress.TEST,
                        azimuth = 50,
                    ),
                )
            ),
            elevation = SensorValue(
                values = listOf(
                    ElevationMeasurementData(
                        quality = QualityIndicator.GOOD,
                        address = PeripheralBluetoothAddress.TEST,
                        elevation = 30,
                    )
                )
            ),
            rttDistance = SensorValue(
                values = listOf(
                    RttMeasurementData(
                        quality = QualityIndicator.POOR,
                        address = PeripheralBluetoothAddress.TEST,
                        rtt = RTTEstimate(10),
                    )
                )
            ),
            mcpdDistance = SensorValue(
                values = listOf(
                    McpdMeasurementData(
                        quality = QualityIndicator.POOR,
                        address = PeripheralBluetoothAddress.TEST,
                        mcpd = MCPDEstimate(
                            ifft = 10,
                            phaseSlope = 15,
                            rssi = 30,
                            best = 25,
                        )
                    )
                )
            )
        ),
        range = 0..50,
    )
}

@Preview(showBackground = true)
@Composable
private fun LinearDataItemViewPreview() {
    LinearDataItemView(
        name = stringResource(R.string.rssi_label),
        range = 0..50,
        item = 40,
        showLegend = true,
    )
}

@Preview(showBackground = true)
@Composable
private fun IfftFullForm() {
    Text(
        text = stringResource(R.string.ifft_hint),
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.fillMaxWidth().alpha(0.5f)
    )
}
