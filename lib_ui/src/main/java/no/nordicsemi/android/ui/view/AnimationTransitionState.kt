package no.nordicsemi.android.ui.view

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearOutSlowInEasing
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
fun createTransition(
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

data class TransitionState(
    val dotRadius: State<Dp>,
    val circleWidth: State<Dp>,
    val circleColor: State<Color>,
    val dotColor: State<Color>,
) {
    fun toggleAccessibilityMode() {
        dotRadius.value
    }
}