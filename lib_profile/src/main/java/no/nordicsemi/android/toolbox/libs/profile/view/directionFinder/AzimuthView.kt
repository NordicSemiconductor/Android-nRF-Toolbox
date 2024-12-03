package no.nordicsemi.android.toolbox.libs.profile.view.directionFinder

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.core.data.SensorData
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.elevation.Range
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.elevation.azimuthValue
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.elevation.distanceValue
import no.nordicsemi.android.ui.view.CircleTransitionState
import no.nordicsemi.android.ui.view.createCircleTransition

@Composable
internal fun AzimuthView(
    sensorData: SensorData,
    range: Range
) {
    val azimuthValue = sensorData.azimuthValue() ?: return
    val distance = sensorData.distanceValue()

    val radius = 100.dp
    val duration = 1000

    val isInAccessibilityMode = rememberSaveable { mutableStateOf(false) }
    val transition = createCircleTransition(isInAccessibilityMode.value, duration)
    val rotationValue = remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastFrame = 0L
        while (isActive) {
            val nextFrame = awaitFrame() / 100_000L
            if (lastFrame != 0L) {
                val period = nextFrame - lastFrame
                rotationValue.value += period / 1000f
            }
            lastFrame = nextFrame
            yield() //TOdo: verify this.
        }
    }

    Box {
        // Render the main canvas
        RenderAzimuthCanvas(
            radius = radius,
            circleBorderColor = MaterialTheme.colorScheme.secondary,
            transition = transition,
            distance = distance,
            isClose = isClose(sensorData, range),
            range = range,
            duration = duration,
        )

        // Render arrow if not close
        if (!isClose(sensorData, range) || distance == null) {
            RenderArrow(
                azimuthValue = azimuthValue,
                dotColor = transition.dotColor.value,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RenderAzimuthCanvas(
    radius: Dp,
    circleBorderColor: Color,
    transition: CircleTransitionState,
    distance: Int?,
    isClose: Boolean,
    range: Range,
    duration: Int,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "InfiniteTransition")
    val scale = infiniteTransition.createInfiniteFloatAnimation(1f, 1.25f, duration)
    val alphaColor = infiniteTransition.createInfiniteFloatAnimation(1f, 0f, duration)
    val rotationAnimatedValue = infiniteTransition.createInfiniteFloatAnimation(0f, 360f, 10_000)

    val progressWidth = calculateProgressWidth(range, distance)
    val rotationOffset = 100.dp * (1f - progressWidth)
    val alphaColorValue = transition.dotColor.value.copy(alpha = alphaColor)

    Canvas(
        modifier = Modifier
            .size(radius * 2)
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {},
                onLongClick = { transition.toggleAccessibilityMode() }
            )
    ) {
        drawOuterCircles(
            radius,
            circleBorderColor,
            transition.circleWidth.value,
            transition.circleColor.value
        )

        if (distance != null) {
            if (isClose) {
                drawCloseDot(radius, transition.dotColor.value, alphaColorValue, scale)
            } else {
                drawRotatingDots(
                    radius,
                    rotationAnimatedValue,
                    rotationOffset,
                    transition.dotColor.value,
                    transition.dotRadius.value
                )
            }
        }
    }
}

private fun DrawScope.drawOuterCircles(
    radius: Dp,
    borderColor: Color,
    width: Dp,
    fillColor: Color
) {
    drawCircle(
        color = borderColor,
        radius = radius.toPx(),
        style = Stroke(width = width.toPx() * 2 + 1)
    )
    drawCircle(
        color = fillColor,
        radius = radius.toPx(),
        style = Stroke(width = width.toPx() * 2)
    )
}

private fun DrawScope.drawCloseDot(
    radius: Dp,
    dotColor: Color,
    alphaColorValue: Color,
    scale: Float
) {
    val center = center
    drawCircle(
        color = dotColor,
        radius = (radius / 2).toPx(),
        center = center
    )
    drawCircle(
        color = alphaColorValue,
        radius = (radius / 2 * scale).toPx(),
        center = center
    )
}

private fun DrawScope.drawRotatingDots(
    radius: Dp,
    rotationValue: Float,
    offset: Dp,
    dotColor: Color,
    dotRadius: Dp
) {
    rotate(rotationValue) {
        repeat(7) { i ->
            rotate(360f / 7 * i) {
                drawCircle(
                    color = dotColor,
                    radius = dotRadius.toPx(),
                    center = Offset(offset.toPx(), radius.toPx())
                )
            }
        }
    }
}

@Composable
private fun RenderArrow(azimuthValue: Int, dotColor: Color, modifier: Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ic_arrow),
        contentDescription = null,
        colorFilter = ColorFilter.tint(dotColor),
        modifier = Modifier
            .rotate(azimuthValue.toFloat())
            .then(modifier)
    )
}

private fun calculateProgressWidth(range: Range, distance: Int?): Float {
    return when {
        distance == null -> 0f
        distance <= range.from -> 0f
        distance >= range.to -> 1f
        else -> (distance.toFloat() - range.from) / (range.to - range.from)
    }
}

@Composable
private fun InfiniteTransition.createInfiniteFloatAnimation(
    initialValue: Float,
    targetValue: Float,
    duration: Int
): Float {
    return animateFloat(
        initialValue = initialValue,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = "Infinite Animation"
    ).value
}

private fun isClose(sensorData: SensorData, range: Range): Boolean {
    val validatedValue = sensorData.distanceValue()?.coerceIn(range.from, range.to) ?: 0
    return validatedValue <= range.from || (validatedValue - range.from) < 10
}

@Preview(showBackground = true)
@Composable
private fun AzimuthViewPreview() {
    AzimuthView(SensorData(), Range(0, 50))
}