package no.nordicsemi.android.toolbox.scanner.view

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.core.parseBold
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.android.common.ui.view.WarningView

@Composable
internal fun ScanEmptyView(locationRequiredAndDisabled: Boolean) {
    WarningView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        imageVector = Icons.AutoMirrored.Filled.BluetoothSearching,
        title = "CAN\\'T SEE YOUR DEVICE?",
        hint = "1. Make sure the device is turned on and is connected to a power source." +
                "\\n\\n2. Make sure the appropriate firmware and SoftDevice are flashed.\n" +
                "   " + if (locationRequiredAndDisabled) {
            "\n\n" + "3. Location is turned off. Most Android phones " +
                    " require it in order to scan for Bluetooth LE devices. If you are sure your " +
                    " device is advertising and it doesn\\'t show up here, click the button below to " +
                    " enable Location"
        } else {
            ""
        }.parseBold(),
        hintTextAlign = TextAlign.Justify,
    ) {
        if (locationRequiredAndDisabled) {
            val context = LocalContext.current
            Button(onClick = { openLocationSettings(context) }) {
                Text(text = "Enable location")
            }
        }
    }
}

private fun openLocationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
}

@Preview(showBackground = true)
@Composable
private fun ScanEmptyViewPreview_RequiredLocation() {
    NordicTheme {
        ScanEmptyView(
            locationRequiredAndDisabled = true,
        )
    }
}

@Preview(device = Devices.TABLET, showBackground = true)
@Composable
private fun ScanEmptyViewPreview() {
    NordicTheme {
        ScanEmptyView(
            locationRequiredAndDisabled = false,
        )
    }
}