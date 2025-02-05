package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.tooling.preview.Preview
import no.nordicsemi.android.toolbox.profile.data.directionFinder.Range
import no.nordicsemi.android.ui.view.createLinearTransition

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DistanceChartView(value: Int, range: Range) {
    val duration = 1000
    val isInAccessibilityMode = rememberSaveable { mutableStateOf(false) }
    val transition = createLinearTransition(isInAccessibilityMode.value, duration)

    BoxWithConstraints {
        Canvas(
            modifier = Modifier
                .height(transition.height.value)
                .fillMaxWidth()
                .border(
                    transition.border.value,
                    transition.color.value,
                    RoundedCornerShape(transition.radius.value)
                )
                .combinedClickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                    },
                    onLongClick = { isInAccessibilityMode.value = !isInAccessibilityMode.value }
                )
        ) {
            drawRoundRect(
                color = transition.inactiveColor.value,
                size = Size(maxWidth.toPx(), transition.height.value.toPx()),
                cornerRadius = CornerRadius(
                    transition.radius.value.toPx(),
                    transition.radius.value.toPx()
                )
            )

            val min = range.from
            val max = range.to
            val progressWidth = when {
                value <= min -> 0f
                value >= max -> 1f
                else -> (value - min).toFloat() / (max - min).toFloat()
            }

            drawRoundRect(
                color = transition.color.value,
                size = Size(progressWidth * size.width, transition.height.value.toPx()),
                cornerRadius = CornerRadius(
                    transition.radius.value.toPx(),
                    transition.radius.value.toPx()
                )
            )
        }
    }
}

@Preview
@Composable
private fun DistanceChartViewPreview() {
    DistanceChartView(20, Range(0, 50))
}