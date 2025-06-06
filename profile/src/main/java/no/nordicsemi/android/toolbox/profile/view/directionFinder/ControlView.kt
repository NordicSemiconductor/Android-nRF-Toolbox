package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.lib.profile.directionFinder.distance.DistanceMode
import no.nordicsemi.android.toolbox.profile.data.DFSServiceData
import no.nordicsemi.android.toolbox.profile.data.SensorData
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.viewmodel.DFSEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.ProfileUiEvent

@Composable
internal fun ControlView(
    viewEntity: DFSServiceData,
    sensorData: SensorData,
    onEvent: (ProfileUiEvent) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when {
            !viewEntity.isDistanceAvailabilityChecked() -> {
                DistanceCheckView {
                    onEvent(DFSEvent.OnAvailableDistanceModeRequest)
                }
            }

            !viewEntity.isDistanceAvailable() -> {
                DistanceNotAvailableView()
            }

            viewEntity.isDoubleModeAvailable() -> {
                CurrentModeView(
                    distanceMode = sensorData.distanceMode,
                    onCheckMode = { onEvent(DFSEvent.OnCheckDistanceModeRequest) },
                    onSwitchMode = { newMode -> onEvent(DFSEvent.OnDistanceModeSelected(newMode)) }
                )
            }

            viewEntity.ddfFeature?.isMcpdAvailable == true -> {
                SingleModeAvailableView(
                    isMcpdAvailable = true,
                )
            }

            viewEntity.ddfFeature?.isRttAvailable == true -> {
                SingleModeAvailableView(
                    isRttAvailable = true,
                )
            }
        }
    }
}

@Composable
private fun DistanceCheckView(onCheckAvailability: () -> Unit) {
    Text(stringResource(id = R.string.check_distance_mode))
    Button(onClick = onCheckAvailability) {
        Text(stringResource(id = R.string.check_availability))
    }
}

@Composable
private fun DistanceNotAvailableView() {
    Text(stringResource(id = R.string.distance_not_available))
}

@Composable
private fun CurrentModeView(
    distanceMode: DistanceMode?,
    onCheckMode: () -> Unit,
    onSwitchMode: (DistanceMode) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(id = R.string.current_mode))
        val modeText = when (distanceMode) {
            DistanceMode.MCPD -> stringResource(id = R.string.mcpd)
            DistanceMode.RTT -> stringResource(id = R.string.rtt)
            null -> stringResource(id = R.string.unknown)
        }
        Text(modeText, style = MaterialTheme.typography.titleMedium)
    }

    when (distanceMode) {
        DistanceMode.MCPD -> {
            Button(onClick = { onSwitchMode(DistanceMode.RTT) }) {
                Text(stringResource(id = R.string.enable_rtt))
            }
        }

        DistanceMode.RTT -> {
            Button(onClick = { onSwitchMode(DistanceMode.MCPD) }) {
                Text(stringResource(id = R.string.enable_mcpd))
            }
        }

        null -> {
            Button(onClick = onCheckMode) {
                Text(stringResource(id = R.string.check_mode))
            }
        }
    }
}

@Composable
private fun SingleModeAvailableView(
    isMcpdAvailable: Boolean? = null,
    isRttAvailable: Boolean? = null
) {
    val messageId = when {
        isMcpdAvailable == true -> R.string.only_mcpd_available
        isRttAvailable == true -> R.string.only_rtt_available
        else -> null
    }
    messageId?.let { Text(stringResource(id = it)) }
}

@Preview(showBackground = true)
@Composable
private fun ControlViewPreview() {
    ControlView(DFSServiceData(), SensorData()) { }
}
