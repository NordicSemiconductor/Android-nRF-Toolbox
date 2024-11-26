package no.nordicsemi.android.toolbox.libs.profile.view.directionFinder

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
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
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import no.nordicsemi.android.toolbox.libs.core.data.azimuthValue
import no.nordicsemi.android.toolbox.libs.core.data.distanceValue
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.Range

@Composable
internal fun AzimuthView(sensorData: SensorData) {
    val azimuthValue = sensorData.azimuthValue() ?: return
    val range = Range(0, 50) // TODO: find the range value
    val distance = sensorData.distanceValue()

    val radius = 100.dp
    val duration = 1000

    // Animation states
    val isInAccessibilityMode = rememberSaveable { mutableStateOf(false) }
    val transition = createTransition(isInAccessibilityMode.value, duration)

    // Infinite animations
    val infiniteTransition = rememberInfiniteTransition()
    val scale = infiniteTransition.createInfiniteFloatAnimation(1f, 1.25f, duration)
    val alphaColor = infiniteTransition.createInfiniteFloatAnimation(1f, 0f, duration)
    val rotationAnimatedValue = infiniteTransition.createInfiniteFloatAnimation(0f, 360f, 10_000)

    val circleBorderColor = MaterialTheme.colorScheme.secondary
    val progressWidth = calculateProgressWidth(range, distance)
    val rotationOffset = 100.dp * (1f - progressWidth)
    // Ensure the `dotColor` is dereferenced for `copy`
    val alphaColorValue = transition.dotColor.value.copy(alpha = alphaColor)

    // Rotation state
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
            yield()
        }
    }

    Box {
        // Render the main canvas
        RenderAzimuthCanvas(
            radius = radius,
            circleBorderColor = circleBorderColor,
            transition = transition,
            distance = distance,
            isClose = isClose(sensorData, range),
            scale = scale,
            alphaColorValue = alphaColorValue,
            rotationAnimatedValue = rotationAnimatedValue,
            rotationOffset = rotationOffset
        )

        // Render arrow if not close
        if (!isClose(sensorData, range) || distance == null) {
            RenderArrow(azimuthValue, transition.dotColor.value)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RenderAzimuthCanvas(
    radius: Dp,
    circleBorderColor: Color,
    transition: TransitionState,
    distance: Int?,
    isClose: Boolean,
    scale: Float,
    alphaColorValue: Color,
    rotationAnimatedValue: Float,
    rotationOffset: Dp
) {
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
private fun RenderArrow(azimuthValue: Int, dotColor: Color) {
    Image(
        painter = painterResource(id = R.drawable.ic_arrow),
        contentDescription = null,
        colorFilter = ColorFilter.tint(dotColor),
        modifier = Modifier
//            .align(Alignment.TopCenter)
            .rotate(azimuthValue.toFloat())
    )
}

// Extension to calculate progress width
private fun calculateProgressWidth(range: Range, distance: Int?): Float {
    return when {
        distance == null -> 0f
        distance <= range.from -> 0f
        distance >= range.to -> 1f
        else -> (distance.toFloat() - range.from) / (range.to - range.from)
    }
}

// Extension for infinite animations
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

// Extension for transition creation
@Composable
private fun createTransition(
    isInAccessibilityMode: Boolean,
    duration: Int
): TransitionState {
    val transition = updateTransition(targetState = isInAccessibilityMode, label = "Transition")
    return TransitionState(
        dotRadius = transition.animateDp(
            label = "Dot Radius",
            transitionSpec = { tween(duration, easing = LinearOutSlowInEasing) }
        ) { if (it) 10.dp else 5.dp },
        circleWidth = transition.animateDp(
            label = "Circle Width",
            transitionSpec = { tween(duration, easing = LinearOutSlowInEasing) }
        ) { if (it) 8.dp else 5.dp },
        circleColor = transition.animateColor(
            label = "Circle Color",
            transitionSpec = { tween(duration, easing = LinearOutSlowInEasing) }
        ) { if (it) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer },
        dotColor = transition.animateColor(
            label = "Dot Color",
            transitionSpec = { tween(duration, easing = LinearOutSlowInEasing) }
        ) { if (it) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary }
    )
}

private data class TransitionState(
    val dotRadius: State<Dp>,
    val circleWidth: State<Dp>,
    val circleColor: State<Color>,
    val dotColor: State<Color>
) {
    fun toggleAccessibilityMode() {
        dotRadius.value
    }
}

private fun isClose(sensorData: SensorData, range: Range): Boolean {
    val validatedValue = sensorData.distanceValue()?.coerceIn(range.from, range.to) ?: 0
    return validatedValue <= range.from || (validatedValue - range.from) < 10
}


@Preview(showBackground = true)
@Composable
private fun AzimuthViewPreview(){
    AzimuthView(SensorData())
}