package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.DFSServiceData
import no.nordicsemi.android.toolbox.profile.data.SensorData
import no.nordicsemi.android.ui.view.ScreenSection

@Composable
internal fun MeasurementDetailsView(
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
private fun MeasurementDetailsViewPreview() {
    MeasurementDetailsView(DFSServiceData(), SensorData())
}
