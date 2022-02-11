package no.nordicsemi.android.theme.view.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.theme.R
import no.nordicsemi.android.theme.view.ScreenSection

enum class Reason {
    USER, LINK_LOSS, MISSING_SERVICE
}

@Composable
fun DeviceDisconnectedView(reason: Reason, navigateUp: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenSection {
            Icon(
                imageVector = Icons.Default.HighlightOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    )
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.size(16.dp))

            Text(
                text = stringResource(id = R.string.device_disconnected),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.size(16.dp))

            val text = when (reason) {
                Reason.USER -> stringResource(id = R.string.device_reason_user)
                Reason.LINK_LOSS -> stringResource(id = R.string.device_reason_link_loss)
                Reason.MISSING_SERVICE -> stringResource(id = R.string.device_reason_missing_service)
            }

            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.size(16.dp))

        Button(onClick = { navigateUp() }) {
            Text(text = stringResource(id = R.string.go_up))
        }
    }
}

@Preview
@Composable
fun DeviceDisconnectedView_Preview() {
    DeviceConnectingView { }
}
