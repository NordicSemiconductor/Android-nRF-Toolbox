package no.nordicsemi.android.toolbox.profile.view.channelSounding

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import no.nordicsemi.android.permissions_ranging.RequestRangingPermission
import no.nordicsemi.android.toolbox.profile.data.ChannelSoundingServiceData

@Composable
fun ChannelSoundingScreen(state: ChannelSoundingServiceData) {
    RequestRangingPermission {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            val context = LocalContext.current
            // check if the RANGING permission is granted
            Text(
                text = "Ranging permission is ${
                    if (ContextCompat.checkSelfPermission(
                            context,
                            "android.permission.RANGING"
                        ) == PackageManager.PERMISSION_GRANTED
                    ) "granted" else "not granted"
                }"
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Profile: ${state.profile}")
        }
    }
}
