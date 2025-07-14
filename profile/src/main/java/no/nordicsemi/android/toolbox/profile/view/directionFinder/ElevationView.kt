package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.lib.profile.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.lib.profile.directionFinder.elevation.ElevationMeasurementData
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.SensorData
import no.nordicsemi.android.toolbox.profile.data.SensorValue
import no.nordicsemi.android.toolbox.profile.data.directionFinder.displayElevation
import no.nordicsemi.android.ui.view.CircleTransitionState
import no.nordicsemi.android.ui.view.createCircleTransition

@Composable
internal fun ElevationView(
    value: Int,
    data: SensorData
) {
    val duration = 1000
    val radius = 100.dp
    val isInAccessibilityMode = rememberSaveable { mutableStateOf(false) }
    val transition = createCircleTransition(isInAccessibilityMode.value, duration)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(8.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier.padding(8.dp)
        ) {
            ElevationLabels(Modifier.padding(8.dp))
            Box(
                modifier = Modifier
                    .size(radius * 2)
                    .align(Alignment.Center) // force centering
            ) {
                ElevationCanvas(
                    radius = radius,
                    circleBorderColor = MaterialTheme.colorScheme.secondary,
                    transition = transition,
                    value = value
                ) {
                    isInAccessibilityMode.value = !isInAccessibilityMode.value
                }
            }
        }
        data.displayElevation()?.let {
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "Tilt Angle: $it",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ElevationViewPreview() {
    val value = 15 // Example elevation value
    val sensorData = SensorData(
        elevation = SensorValue(
            values = listOf(
                ElevationMeasurementData(
                    address = PeripheralBluetoothAddress.TEST,
                    elevation = 30
                )
            )
        )
    )
    ElevationView(value = value, sensorData)
}

@Composable
private fun BoxScope.ElevationLabels(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(id = R.string.elevation_max),
        modifier = modifier.align(Alignment.TopCenter)
    )
    Text(
        text = stringResource(id = R.string.elevation_medium),
        modifier = modifier.align(Alignment.CenterEnd)
    )
    Text(
        text = stringResource(id = R.string.elevation_min),
        modifier = modifier.align(Alignment.BottomCenter)
    )
}

@Composable
private fun ElevationCanvas(
    radius: Dp,
    circleBorderColor: Color,
    transition: CircleTransitionState,
    value: Int,
    onLongClick: () -> Unit
) {
    Canvas(
        modifier = Modifier
            .requiredSize(radius * 2)
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {},
                onLongClick = onLongClick
            )
    ) {
        drawArc(
            color = circleBorderColor,
            startAngle = -90f,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(
                width = (transition.circleWidth.value * 2 + 1.dp).toPx(),
                join = StrokeJoin.Round,
                cap = StrokeCap.Round
            )
        )

        drawArc(
            color = transition.circleColor.value,
            startAngle = -90f,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(
                width = (transition.circleWidth.value * 2).toPx(),
                join = StrokeJoin.Round,
                cap = StrokeCap.Round
            )
        )

        rotate(90f - value.toFloat()) {
            drawCircle(
                color = transition.dotColor.value,
                radius = transition.dotRadius.value.toPx(),
                center = Offset(x = size.width / 2, y = 0f)
            )
        }
    }
}
