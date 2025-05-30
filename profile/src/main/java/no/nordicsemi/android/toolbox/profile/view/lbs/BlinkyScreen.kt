package no.nordicsemi.android.toolbox.profile.view.lbs

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.data.LBSData
import no.nordicsemi.android.toolbox.profile.data.LBSServiceData
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.LBSViewEvent

@Composable
internal fun BlinkyScreen(
    serviceData: LBSServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        serviceData.data?.ledState?.let { ledState ->
            LedControlView(
                ledState = ledState,
                onStateChanged = { onClickEvent(LBSViewEvent.OnLedStateChanged(it)) },
            )
        }

        serviceData.data?.buttonState?.let { buttonState ->
            ButtonControlView(
                buttonState = buttonState,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BlinkyScreenPreview() {
    BlinkyScreen(
        serviceData = LBSServiceData(data = LBSData(ledState = true, buttonState = false)),
        onClickEvent = {}
    )
}

@Composable
private fun ButtonControlView(
    buttonState: Boolean
) {
    OutlinedCard {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    imageVector = Icons.Default.RadioButtonChecked,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 16.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
                Text(
                    text = "Button",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Button State: ",
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (buttonState) "PRESSED" else "RELEASED",
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ButtonControlViewPreview() {
    ButtonControlView(
        buttonState = true,
    )
}

@Composable
private fun LedControlView(
    ledState: Boolean,
    onStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .clickable { onStateChanged(!ledState) }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 16.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
                Text(
                    text = "Light",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Led is ${if (ledState) "ON" else "OFF"}",
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = ledState, onCheckedChange = onStateChanged)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LecControlViewPreview() {
    LedControlView(
        ledState = true,
        onStateChanged = {},
        modifier = Modifier.padding(16.dp),
    )
}
