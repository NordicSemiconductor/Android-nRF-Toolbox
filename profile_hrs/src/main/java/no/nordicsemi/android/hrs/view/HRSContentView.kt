package no.nordicsemi.android.hrs.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.hrs.R
import no.nordicsemi.android.hrs.data.HRSData
import no.nordicsemi.android.material.you.ScreenSection
import no.nordicsemi.android.theme.view.BatteryLevelView
import no.nordicsemi.android.theme.view.SectionTitle

@Composable
internal fun HRSContentView(state: HRSData, onEvent: (HRSScreenViewEvent) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        ScreenSection {
            SectionTitle(resId = R.drawable.ic_chart_line, title = "Data")

            Spacer(modifier = Modifier.height(16.dp))

            LineChartView(state)
        }

        Spacer(modifier = Modifier.height(16.dp))

        state.batteryLevel?.let {
            BatteryLevelView(it)

            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = { onEvent(DisconnectEvent) }
        ) {
            Text(text = stringResource(id = R.string.disconnect))
        }
    }
}

@Preview
@Composable
private fun Preview() {
    HRSContentView(state = HRSData()) { }
}
