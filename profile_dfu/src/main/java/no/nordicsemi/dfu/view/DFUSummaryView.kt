package no.nordicsemi.dfu.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.theme.view.ScreenSection
import no.nordicsemi.android.theme.view.SectionTitle
import no.nordicsemi.dfu.R
import no.nordicsemi.dfu.data.FileReadyState
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import java.io.File

@Composable
internal fun DFUSummaryView(state: FileReadyState, onEvent: (DFUViewEvent) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DeviceDetailsView(state.device)
        
        Spacer(modifier = Modifier.height(16.dp))

        FileDetailsView(state.file)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onEvent(OnInstallButtonClick) }) {
            Text(text = stringResource(id = R.string.dfu_install))
        }
    }
}

@Composable
internal fun DeviceDetailsView(device: DiscoveredBluetoothDevice) {
    ScreenSection {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_bluetooth),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    )
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Column(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)) {
                Text(
                    text = device.displayName(),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = device.displayAddress(), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun FileDetailsView(file: File) {
    val fileName = file.name
    val fileLength = file.length()

    ScreenSection {
        SectionTitle(icon = Icons.Default.Notifications, title = stringResource(id = R.string.dfu_file_details))

        Spacer(modifier = Modifier.padding(16.dp))

        Text(text = fileName)

        Spacer(modifier = Modifier.padding(4.dp))
        
        Text(text = stringResource(id = R.string.dfu_file_size, fileLength))
    }
}
