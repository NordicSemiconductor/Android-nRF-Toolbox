package no.nordicsemi.android.toolbox.profile.view.channelSounding

import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SocialDistance
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.permissions_ranging.RequestRangingPermission
import no.nordicsemi.android.toolbox.profile.viewmodel.ChannelSoundingViewModel
import no.nordicsemi.android.ui.view.SectionTitle

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
@Composable
internal fun ChannelSoundingScreen() {
    val channelSoundingViewModel = hiltViewModel<ChannelSoundingViewModel>()
    val channelSoundingState by channelSoundingViewModel.channelSoundingState.collectAsStateWithLifecycle()

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
                if (ContextCompat.checkSelfPermission(
                        context,
                        "android.permission.RANGING"
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    "Ranging permission is granted"
                } else {
                    "Ranging permission is not granted"
                }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Text(rangingPermissionStatusMessage)
                channelSoundingState.rangingData?.let { rangingData ->
                    val measurement = rangingData.distance?.measurement
                    val confidence = rangingData.distance?.confidence


                    Text(
                        text = "Distance: ${if (measurement != null) "$measurement m" else "null"}, " + "\n" +
                                "Confidence: ${
                                    if (confidence != null) "${confidence.toConfidenceLevel()} " else "null"
                                }" + "\n" +
                                "RangingTechnology: ${
                                    RangingTechnology.displayString(
                                        rangingData.rangingTechnology
                                    )
                                }\n" +
                                "Azimuth: ${rangingData.azimuth}\nElevation: ${rangingData.elevation}"

                    )
                }
            }
        }
    }
}

internal fun Int.toConfidenceLevel(): String? {
    return when (this) {
        2 -> "CONFIDENCE_HIGH"
        1 -> "CONFIDENCE_MEDIUM"
        0 -> "CONFIDENCE_LOW"
        else -> "Unknown"
    }
}

enum class RangingTechnology(val value: Int) {
    BLE_CS(1),
    BLE_RSSI(3),
    UWB(0),
    WIFI_NAN_RTT(2),
    WIFI_STA_RTT(4), ;

    override fun toString(): String {
        return when (this) {
            BLE_CS -> "BLE_CS"
            BLE_RSSI -> "BLE_RSSI"
            UWB -> "UWB"
            WIFI_NAN_RTT -> "WIFI_NAN_RTT"
            WIFI_STA_RTT -> "WIFI_STA_RTT"
        }
    }

    companion object {
        fun from(value: Int): RangingTechnology? = entries.find { it.value == value }

        fun displayString(value: Int): String {
            return from(value)?.toString() ?: "Unknown"
        }
    }
}


