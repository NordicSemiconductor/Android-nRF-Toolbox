package no.nordicsemi.android.ui.view.internal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.ui.view.CircularIcon

enum class DisconnectReason {
    USER, UNKNOWN, LINK_LOSS, MISSING_SERVICE, BLUETOOTH_OFF
}

@Composable
fun DeviceDisconnectedView(
    reason: DisconnectReason,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(PaddingValues) -> Unit = {},
) {
    val disconnectedReason = when (reason) {
        DisconnectReason.USER -> "Device disconnected successfully."
        DisconnectReason.UNKNOWN -> "Device disconnected with unknown reason."
        DisconnectReason.LINK_LOSS -> "Device signal has been lost."
        DisconnectReason.MISSING_SERVICE -> "Missing required service on the device."
        DisconnectReason.BLUETOOTH_OFF -> "Bluetooth adapter is turned off."
    }

    DeviceDisconnectedView(
        disconnectedReason = disconnectedReason,
        modifier = modifier,
        content = content,
        isMissingService = reason == DisconnectReason.MISSING_SERVICE
    )
}

@Composable
fun DeviceDisconnectedView(
    disconnectedReason: String,
    modifier: Modifier = Modifier,
    isMissingService: Boolean = false,
    content: @Composable ColumnScope.(PaddingValues) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedCard(
            modifier = Modifier
                .widthIn(max = 460.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularIcon(imageVector = Icons.Default.HighlightOff)

                Text(
                    text = if (isMissingService) "No supported services" else "Device disconnected",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = disconnectedReason,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        content(PaddingValues(top = 16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceDisconnectedViewPreview() {
    MaterialTheme {
        DeviceDisconnectedView(
            reason = DisconnectReason.LINK_LOSS,
            content = { padding ->
                Button(
                    onClick = {},
                    modifier = Modifier.padding(padding)
                ) {
                    Text(text = "Retry")
                }
            }
        )
    }
}
