package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.lib.profile.directionFinder.distance.DistanceMode
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.DFSServiceData
import no.nordicsemi.android.toolbox.profile.data.SensorData
import no.nordicsemi.android.toolbox.profile.data.uart.MacroEol
import no.nordicsemi.android.toolbox.profile.viewmodel.DFSEvent

@Composable
internal fun ControlView(
    viewEntity: DFSServiceData,
    sensorData: SensorData,
    onEvent: (DFSEvent) -> Unit
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

@Preview(showBackground = true)
@Composable
private fun DistanceCheckViewPreview() {
    DistanceCheckView {}
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
    if (distanceMode == null) {
        Button(onClick = onCheckMode) {
            Text(stringResource(id = R.string.check_mode))
        }
    } else {
        Box(
            modifier = Modifier.clip(RoundedCornerShape(8.dp)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                DistanceMode.entries.forEachIndexed { index, it ->
                    val selected = it == distanceMode
                    val clip = if (selected) RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)
                    val (color, textColor) = if (selected) {
                        MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer to MaterialTheme.colorScheme.onSurface
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(clip)
                            .background(color = color)
                            .clickable { onSwitchMode(it) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            it.toString(),
                            modifier = Modifier.padding(8.dp),
                            color = textColor,
                        )
                    }
                    if ((index < MacroEol.entries.size - 1) && !selected)
                        VerticalDivider(
                            modifier = Modifier
                                .height(IntrinsicSize.Max)
                                .background(MaterialTheme.colorScheme.onSurface)
                        )
                }
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
private fun SingleModeAvailableViewPreview() {
    SingleModeAvailableView(false, true)
}


@Preview(showBackground = true)
@Composable
private fun ControlViewPreview() {
    CurrentModeView(DistanceMode.MCPD, {}) { }
}
