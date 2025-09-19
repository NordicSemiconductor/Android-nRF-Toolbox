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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.permissions_ranging.RequestRangingPermission
import no.nordicsemi.android.toolbox.profile.data.ChannelSoundingServiceData
import no.nordicsemi.android.toolbox.profile.data.ConfidenceLevel
import no.nordicsemi.android.toolbox.profile.data.RangingSessionAction
import no.nordicsemi.android.toolbox.profile.data.RangingTechnology
import no.nordicsemi.android.toolbox.profile.viewmodel.ChannelSoundingEvent
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
    val onClickEvent: (event: ChannelSoundingEvent) -> Unit =
        { channelSoundingViewModel.onEvent(it) }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        RequestRangingPermission {
            ChannelSoundingView(channelSoundingState, onClickEvent)
        }
    } else {
        ChannelSoundingNotSupportedView()
    }
}

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
private fun ChannelSoundingView(
    channelSoundingState: ChannelSoundingServiceData,
    onClickEvent: (ChannelSoundingEvent) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ScreenSection(modifier = Modifier.padding(0.dp) /* No padding */) {
            Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
                SectionTitle(
                    icon = Icons.Default.SocialDistance,
                    title = "Channel Sounding",
                    menu = {
                        UpdateRateSettings(
                            selectedItem = channelSoundingState.updateRate,
                            onItemSelected = {
                                onClickEvent(
                                    ChannelSoundingEvent.RangingUpdateRate(it)
                                )
                            }
                        )
                    }
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
                            Text(
                                "Ranging session closed because of ${sessionData.reason}.",
                                modifier = Modifier.padding(8.dp)
                            )
                        } else {
                            Text(
                                "Ranging session closed.",
                                modifier = Modifier.padding(8.dp)
                            )
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
private fun RangingContent(
    rangingData: RangingData,
) {
    val measurement = rangingData.distance?.measurement
    val confidence = rangingData.distance?.confidence
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        measurement?.let { measurement ->
            val distance = String.format(Locale.US, "%.2f", measurement)
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
                value = "Ranging technology",
                key = RangingTechnology.displayString(rangingData.rangingTechnology),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
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
