package no.nordicsemi.android.toolbox.profile.view.dfu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.viewmodel.DFUViewModel

@Composable
internal fun DFUScreen() {
    val dfuViewModel = hiltViewModel<DFUViewModel>()
    val dfuServiceState by dfuViewModel.dfuServiceState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    dfuServiceState.dfuAppName?.let { dfuApp ->
        val intent = context.packageManager.getLaunchIntentForPackage(dfuApp.packageName)
        val description = intent?.let { "Open ${dfuApp.appName}" } ?: "Download from Play Store"

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedCard {
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
                        modifier = Modifier.size(56.dp)
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

            Button(
                onClick = {
                    intent?.let { context.startActivity(it) }
                        ?: uriHandler.openUri(dfuApp.appLink)
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = intent?.let { dfuApp.appIcon } ?: R.drawable.google_play_2022_icon
                    val size = if (intent != null) 56.dp else 28.dp

                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(size)
                            .padding(end = 8.dp),
                        tint = if (intent == null) Color.Unspecified else MaterialTheme.colorScheme.onPrimary
                    )

                    Text(text = description)
                }
            }
        }
    }
}
