package no.nordicsemi.android.toolbox.profile.view.channelSounding

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SocialDistance
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import no.nordicsemi.android.permissions_ranging.RequestRangingPermission
import no.nordicsemi.android.toolbox.profile.data.ChannelSoundingServiceData
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun ChannelSoundingScreen(state: ChannelSoundingServiceData) {
    RequestRangingPermission {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .padding(16.dp)
        ) {
            SectionTitle(
                icon = Icons.Default.SocialDistance,
                title = "Channel Sounding",
            )
            val context = LocalContext.current
            val rangingPermissionStatusMessage =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            "android.permission.RANGING"
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        "Ranging permission is granted"
                    } else {
                        "Ranging permission is not granted"
                    }
                } else {
                    "Channel Sounding Service is not available on this Android version."
                }

            Box(contentAlignment = Alignment.Center) {

                Text(
                    text = rangingPermissionStatusMessage
                )
            }
        }
    }
}
