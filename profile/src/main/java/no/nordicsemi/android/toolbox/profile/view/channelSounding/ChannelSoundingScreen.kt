package no.nordicsemi.android.toolbox.profile.view.channelSounding

import android.os.Build
import android.ranging.RangingData
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SocialDistance
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.permissions_ranging.RequestRangingPermission
import no.nordicsemi.android.toolbox.profile.data.ChannelSoundingServiceData
import no.nordicsemi.android.toolbox.profile.data.RangingSessionAction
import no.nordicsemi.android.toolbox.profile.viewmodel.ChannelSoundingViewModel
import no.nordicsemi.android.ui.view.KeyValueColumn
import no.nordicsemi.android.ui.view.KeyValueColumnReverse
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionRow
import no.nordicsemi.android.ui.view.SectionTitle
import no.nordicsemi.android.ui.view.TextWithAnimatedDots
import no.nordicsemi.android.ui.view.animate.AnimatedDistance
import no.nordicsemi.android.ui.view.internal.LoadingView
import java.util.Locale

@Composable
internal fun ChannelSoundingScreen() {
    val channelSoundingViewModel = hiltViewModel<ChannelSoundingViewModel>()
    val channelSoundingState by channelSoundingViewModel.channelSoundingState.collectAsStateWithLifecycle()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        RequestRangingPermission {
            ChannelSoundingView(channelSoundingState)
        }
    } else {
        ChannelSoundingNotSupportedView()
    }
}

@Preview(showBackground = true)
@Composable
private fun ChannelSoundingNotSupportedView() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ScreenSection(modifier = Modifier.padding(0.dp) /* No padding */) {
            Column(
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
            ) {
                SectionTitle(
                    icon = Icons.Default.SocialDistance,
                    title = "Channel Sounding",
                )
            }
            Text("Channel Sounding is not supported on this Android version.")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
@Composable
private fun ChannelSoundingView(channelSoundingState: ChannelSoundingServiceData) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ScreenSection(modifier = Modifier.padding(0.dp) /* No padding */) {
            Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
                SectionTitle(
                    icon = Icons.Default.SocialDistance,
                    title = "Channel Sounding",
                )
            }
            when (val sessionData = channelSoundingState.rangingSessionAction) {
                is RangingSessionAction.OnError -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (sessionData.reason.isNotEmpty()) {
                            Text("Ranging session closed because of ${sessionData.reason}.")
                        } else {
                            Text("Ranging session closed.")
                        }
                    }
                }

                is RangingSessionAction.OnResult -> {
                    RangingContent(sessionData.data)
                }

                RangingSessionAction.OnClosed -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Ranging stopped")
                    }
                }

                RangingSessionAction.OnStart -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        TextWithAnimatedDots(
                            text = "Initiating ranging",
                        )
                    }
                }

                null -> LoadingView()
            }

        }
    }
}

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
@Composable
private fun RangingContent(rangingData: RangingData) {
    val measurement = rangingData.distance?.measurement
    val confidence = rangingData.distance?.confidence
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        measurement?.let {
            val distance = String.format(Locale.US, "%.2f", it)
            SectionRow {
                KeyValueColumn(
                    value = "Distance",
                    key = "$distance m",
                )
                confidence?.let {
                    KeyValueColumnReverse(
                        value = "Signal strength",
                        key = ConfidenceLevel.displayString(it),
                    )
                }
            }
        }
        SectionRow {
            KeyValueColumn(
                value = "Ranging Technology",
                key = RangingTechnology.displayString(rangingData.rangingTechnology),
            )
        }

        measurement?.let {
            ShowRangingMeasurement(it)
        }
    }
}

@Composable
private fun ShowRangingMeasurement(measurement: Double) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val distance = String.format(Locale.US, "%.2f", measurement)

        Spacer(modifier = Modifier.height(8.dp))
        RangingChartView(measurement = measurement.toFloat())
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedDistance(
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = " Distance $distance m",
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShowRangingMeasurementPreview() {
    ShowRangingMeasurement(12.00)
}

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
@Preview(showBackground = true)
@Composable
private fun ChannelSoundingViewPreview() {
    val sampleData = ChannelSoundingServiceData(
        rangingSessionAction = RangingSessionAction.OnStart
    )
    ChannelSoundingView(sampleData)
}

internal enum class RangingTechnology(val value: Int) {
    BLE_CS(1),
    BLE_RSSI(3),
    UWB(0),
    WIFI_NAN_RTT(2),
    WIFI_STA_RTT(4), ;

    override fun toString(): String {
        return when (this) {
            BLE_CS -> "Bluetooth LE Channel Sounding"
            BLE_RSSI -> "Bluetooth LE RSSI"
            UWB -> "UWB"
            WIFI_NAN_RTT -> "Wifi NAN RTT"
            WIFI_STA_RTT -> "Wifi STA RTT"
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
            CONFIDENCE_HIGH -> "High"
            CONFIDENCE_MEDIUM -> "Medium"
            CONFIDENCE_LOW -> "Low"
        }
    }

    companion object {
        fun from(value: Int): ConfidenceLevel? = entries.find { it.value == value }

        fun displayString(value: Int): String {
            return from(value)?.toString() ?: "Unknown"
        }
    }
}


