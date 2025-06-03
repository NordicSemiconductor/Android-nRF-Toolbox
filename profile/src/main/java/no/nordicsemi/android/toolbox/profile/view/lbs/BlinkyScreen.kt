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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.R
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
        LedControlView(
            ledState = serviceData.data.ledState,
            onStateChanged = { onClickEvent(LBSViewEvent.OnLedStateChanged(it)) },
        )

        ButtonControlView(
            buttonState = serviceData.data.buttonState,
        )
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
    val (text, textColor) = if (buttonState) {
        stringResource(id = R.string.button_pressed) to MaterialTheme.colorScheme.primary
    } else {
        stringResource(id = R.string.button_released) to MaterialTheme.colorScheme.onSurface
    }
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
                    colorFilter = ColorFilter.tint(textColor)
                )
                Text(
                    text = stringResource(id = R.string.button),
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
                    text = text,
                    color = textColor,
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
    val colorFilter = if (ledState) {
        ColorFilter.tint(MaterialTheme.colorScheme.primary)
    } else {
        ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
    }
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
                    colorFilter = colorFilter
                )
                Text(
                    text = stringResource(id = R.string.light),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.led_guide),
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
