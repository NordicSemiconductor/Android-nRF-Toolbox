package no.nordicsemi.android.prx.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.prx.R
import no.nordicsemi.android.theme.R as themeR
import no.nordicsemi.android.theme.view.ScreenSection
import androidx.compose.material.icons.filled.HighlightOff

@Composable
fun DeviceOutOfRangeView(navigateUp: () -> Unit) {
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
                text = stringResource(id = R.string.prx_device_out_of_range),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.size(16.dp))

            Text(
                text = stringResource(id = R.string.prx_device_out_of_range_reason),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.size(16.dp))

        Button(onClick = { navigateUp() }) {
            Text(text = stringResource(id = themeR.string.disconnect))
        }
    }
}
