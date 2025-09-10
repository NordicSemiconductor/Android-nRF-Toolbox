package no.nordicsemi.android.toolbox.profile.view.channelSounding

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import no.nordicsemi.android.toolbox.profile.data.directionFinder.Range
import no.nordicsemi.android.toolbox.profile.view.directionFinder.DistanceChartView
import no.nordicsemi.android.toolbox.profile.viewmodel.ChannelSoundingViewModel
import no.nordicsemi.android.ui.view.SectionTitle
import java.util.Locale

@Composable
internal fun ChannelSoundingScreen() {
    val channelSoundingViewModel = hiltViewModel<ChannelSoundingViewModel>()
    val channelSoundingState by channelSoundingViewModel.channelSoundingState.collectAsStateWithLifecycle()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
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
                                        if (confidence != null)
                                            "${ConfidenceLevel.displayString(confidence)} " else "null"
                                    }" + "\n" +
                                    "RangingTechnology: ${
                                        RangingTechnology.displayString(
                                            rangingData.rangingTechnology
                                        )
                                    }\n" +
                                    "Azimuth: ${rangingData.azimuth}\nElevation: ${rangingData.elevation}"
                        )

                        measurement?.let {
                            val rangeMax =
                                if (it < 20) 20
                                else if (it < 50) 50
                                else if (it < 100) 100
                                else if (it < 200) 200
                                else if (it < 500) 500
                                else 1000

                            Column {
                                DistanceChartView(value = it.toInt(), range = Range(0, rangeMax))
                                Spacer(modifier = Modifier.padding(4.dp))

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = String.format(Locale.US, "%d m", 0))

                                    val diff = rangeMax - 0
                                    val part = (diff / 4)
                                    Text(text = String.format(Locale.US, "%d m", 0 + part))
                                    Text(text = String.format(Locale.US, "%d m", 0 + 2 * part))

                                    Text(text = String.format(Locale.US, "%d m", rangeMax))
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Channel Sounding is not supported on this Android version.")
        }
    }
}

internal enum class RangingTechnology(val value: Int) {
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

internal enum class ConfidenceLevel(val value: Int) {
    CONFIDENCE_HIGH(2),
    CONFIDENCE_MEDIUM(1),
    CONFIDENCE_LOW(0);

    override fun toString(): String {
        return when (this) {
            CONFIDENCE_HIGH -> "CConfidence High"
            CONFIDENCE_MEDIUM -> "CConfidence Medium"
            CONFIDENCE_LOW -> "CConfidence Low"
        }
    }

    companion object {
        fun from(value: Int): ConfidenceLevel? = entries.find { it.value == value }

        fun displayString(value: Int): String {
            return from(value)?.toString() ?: "Unknown"
        }
    }
}


