package no.nordicsemi.android.ui.view

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun createCircleTransition(
    isInAccessibilityMode: Boolean,
    duration: Int
): CircleTransitionState {
    val transition = updateTransition(targetState = isInAccessibilityMode, label = "Transition")
    return CircleTransitionState(
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

data class CircleTransitionState(
    val dotRadius: State<Dp>,
    val circleWidth: State<Dp>,
    val circleColor: State<Color>,
    val dotColor: State<Color>,
) {
    fun toggleAccessibilityMode() {
        dotRadius.value
    }
}

data class LinearTransitionState(
    val border: State<Dp>,
    val height: State<Dp>,
    val radius: State<Dp>,
    val color: State<Color>,
    val inactiveColor: State<Color>,
)

@Composable
fun createLinearTransition(
    isInAccessibilityMode: Boolean,
    duration: Int,
): LinearTransitionState {
    val transition = updateTransition(targetState = isInAccessibilityMode, label = "Transition")
    return LinearTransitionState(
        border = transition.animateDp(
            label = "Border",
            transitionSpec = { TweenSpec(duration, 0, LinearOutSlowInEasing) }
        ) { if (it) 3.dp else 0.dp },

        height = transition.animateDp(
            label = "Height",
            transitionSpec = { TweenSpec(duration, 0, LinearOutSlowInEasing) }
        ) { if (it) 30.dp else 25.dp },
        radius = transition.animateDp(
            label = "Radius",
            transitionSpec = { TweenSpec(duration / 2, 0, LinearOutSlowInEasing) }
        ) { if (it) 4.dp else 8.dp },
        color = transition.animateColor(
            label = "Color",
            transitionSpec = { TweenSpec(duration, 0, LinearOutSlowInEasing) }
        ) {
            if (it) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
        },
        inactiveColor = transition.animateColor(
            label = "In-active color",
            transitionSpec = { TweenSpec(duration, 0, LinearOutSlowInEasing) }
        ) { if (it) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer },
    )
}

@Composable
fun createAngularTransition(
    isInAccessibilityMode: Boolean,
    duration: Int,
): ChartTransition {
    val transition = updateTransition(
        targetState = isInAccessibilityMode,
        label = "Accessibility transition"
    )
    return ChartTransition(
        height = transition.animateDp(
            label = "Height",
            transitionSpec = { TweenSpec(duration, 0, LinearOutSlowInEasing) }
        ) { if (it) 100.dp else 50.dp },
        avgLineWidth = transition.animateDp(
            label = "Average Line Width",
            transitionSpec = { TweenSpec(duration, 0, LinearOutSlowInEasing) }
        ) { if (it) 8.dp else 2.dp },
        chartColor = transition.animateColor(
            label = "Chart Color",
            transitionSpec = { TweenSpec(duration, 0, LinearOutSlowInEasing) }
        ) { if (it) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary },
        avgLineColor = transition.animateColor(
            label = "Average Line Color",
            transitionSpec = { TweenSpec(duration, 0, LinearOutSlowInEasing) }
        ) { if (it) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary }
    )
}

data class ChartTransition(
    val height: State<Dp>,
    val avgLineWidth: State<Dp>,
    val chartColor: State<Color>,
    val avgLineColor: State<Color>,
)
