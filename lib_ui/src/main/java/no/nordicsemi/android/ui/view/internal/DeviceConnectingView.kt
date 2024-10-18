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
import androidx.compose.material.icons.filled.HourglassTop
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

@Composable
fun DeviceConnectingView(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(PaddingValues) -> Unit = {}
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
                CircularIcon(imageVector = Icons.Default.HourglassTop)

                Text(
                    text = "Connecting...",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "The mobile is trying to connect to peripheral device.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Please wait...",
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
private fun DeviceConnectingView_Preview() {
    MaterialTheme {
        DeviceConnectingView { padding ->
            Button(
                onClick = {},
                modifier = Modifier.padding(padding)
            ) {
                Text(text = "Cancel")
            }
        }
    }
}
