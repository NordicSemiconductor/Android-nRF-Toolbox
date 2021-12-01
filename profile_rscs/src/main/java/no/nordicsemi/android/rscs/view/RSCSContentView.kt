package no.nordicsemi.android.rscs.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.rscs.R
import no.nordicsemi.android.rscs.data.RSCSData
import no.nordicsemi.android.theme.view.BatteryLevelView

@Composable
internal fun RSCSContentView(state: RSCSData, onEvent: (RSCScreenViewEvent) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        SensorsReadingView(state = state)

        Spacer(modifier = Modifier.height(16.dp))

        BatteryLevelView(state.batteryLevel)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onEvent(DisconnectEvent) }
        ) {
            Text(text = stringResource(id = R.string.disconnect))
        }
    }
}

@Preview
@Composable
private fun RSCSContentViewPreview() {
    RSCSContentView(RSCSData()) { }
}
