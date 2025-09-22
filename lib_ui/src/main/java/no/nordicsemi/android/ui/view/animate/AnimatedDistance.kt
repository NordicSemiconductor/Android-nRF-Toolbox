package no.nordicsemi.android.ui.view.animate

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SocialDistance
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true)
@Composable
fun AnimatedDistance(
    modifier: Modifier = Modifier,
    color: Color = Color.Blue
) {
    // Infinite transition for pulsing animation
    val infiniteTransition = rememberInfiniteTransition()

    val scaleX by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )

    Icon(
        imageVector = Icons.Filled.SocialDistance,
        contentDescription = "Distance icon",
        modifier = modifier
            .size(28.dp)
            .graphicsLayer(
                // Scale only horizontally
                scaleX = scaleX,
                scaleY = 1f,
            ),
        tint = color
    )
}