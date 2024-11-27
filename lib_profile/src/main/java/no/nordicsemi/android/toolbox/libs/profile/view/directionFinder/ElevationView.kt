package no.nordicsemi.android.toolbox.libs.profile.view.directionFinder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.ui.view.TransitionState
import no.nordicsemi.android.ui.view.createTransition

@Composable
internal fun ElevationView(value: Int) {
    val duration = 1000
    val radius = 100.dp
    val isInAccessibilityMode = rememberSaveable { mutableStateOf(false) }
    val transition = createTransition(isInAccessibilityMode.value, duration)

    Box {
        ElevationLabels()
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

@Composable
private fun BoxScope.ElevationLabels() {
    Text(
        text = stringResource(id = R.string.elevation_max),
        modifier = Modifier.align(Alignment.TopCenter)
    )
    Text(
        text = stringResource(id = R.string.elevation_medium),
        modifier = Modifier.align(Alignment.CenterEnd)
    )
    Text(
        text = stringResource(id = R.string.elevation_min),
        modifier = Modifier.align(Alignment.BottomCenter)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ElevationCanvas(
    radius: Dp,
    circleBorderColor: Color,
    transition: TransitionState,
    value: Int,
    onLongClick: () -> Unit
) {
    Canvas(
        modifier = Modifier
            .size(radius * 2)
            .padding(30.dp)
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
