package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.libs.core.data.SensorData
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.Range
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.bestEffortValue
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.ifftValue
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.isMcpdSectionAvailable
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.phaseSlopeValue
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.rssiValue
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.rttValue

@Composable
internal fun LinearDataView(
    data: SensorData,
    range: Range
) {
    Column {
        data.rttValue()?.let {
            Text(stringResource(id = R.string.rtt), style = MaterialTheme.typography.titleSmall)

            Spacer(modifier = Modifier.padding(8.dp))

            LinearDataItemView(name = stringResource(id = R.string.rtt_label), range, it)

            Spacer(modifier = Modifier.padding(8.dp))
        }

        if (data.isMcpdSectionAvailable()) {
            Text(stringResource(id = R.string.mcpd), style = MaterialTheme.typography.titleSmall)

            Spacer(modifier = Modifier.padding(8.dp))
        }

        data.ifftValue()?.let {
            LinearDataItemView(name = stringResource(id = R.string.ifft_label), range, it)

            Spacer(modifier = Modifier.padding(8.dp))
        }

        data.phaseSlopeValue()?.let {
            LinearDataItemView(name = stringResource(id = R.string.phase_label), range, it)

            Spacer(modifier = Modifier.padding(8.dp))
        }

        data.rssiValue()?.let {
            LinearDataItemView(name = stringResource(id = R.string.rssi_label), range, it)

            Spacer(modifier = Modifier.padding(8.dp))
        }

        data.bestEffortValue()?.let {

            LinearDataItemView(name = stringResource(id = R.string.best_label), range, it)

            Spacer(modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
private fun LinearDataItemView(name: String, range: Range, item: Int) {
    val labelWidth = 40.dp

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.width(labelWidth),
                text = name,
                style = MaterialTheme.typography.labelSmall
            )
            DistanceChartView(value = item, range = range)
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = labelWidth)
        ) {
            Text(
                text = stringResource(R.string.dm_value, range.from),
                style = MaterialTheme.typography.labelSmall
            )

            val diff = range.to - range.from
            val part = (diff / 4)
            if (part > 0) {
                Text(
                    text = stringResource(R.string.dm_value, range.from + part),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = stringResource(R.string.dm_value, range.from + 2 * part),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Text(
                text = stringResource(R.string.dm_value, range.to),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Preview
@Composable
private fun LinearDataItemViewPreview() {
    LinearDataItemView(
        name = "Linear Data Item View",
        range = Range(0, 50),
        item = 49
    )
}
