package no.nordicsemi.android.toolbox.profile.view.dfu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.android.toolbox.profile.R

@Composable
internal fun DFUScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                Icon(
                    painter = painterResource(R.drawable.ic_dfu),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = "DFU is not supported",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "DFU service is not available in the current version of the app. " +
                            "Please use the DFU app from Nordic Semiconductor to update your device’s firmware.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        val uriHandler = LocalUriHandler.current
        val context = LocalContext.current
        val packageManger = context.packageManager
        val intent = packageManger.getLaunchIntentForPackage(DFU_PACKAGE_NAME)

        val description = intent?.let {
            "Open DFU"
        } ?: "Download from Play Store"

        Button(
            onClick = {
                if (intent != null) {
                    context.startActivity(intent)
                } else {
                    uriHandler.openUri(DFU_APP_LINK)
                }
            },
        ) {
            Row {
                intent?.let {
                    Icon(
                        painter = painterResource(R.drawable.ic_dfu),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp)
                    )
                } ?: Icon(imageVector = Icons.Default.Download, contentDescription = null)
                Text(text = description)
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceDisconnectedViewPreview() {
    NordicTheme {
        DFUScreen(
            modifier = Modifier.padding(16.dp)
        )
    }
}
