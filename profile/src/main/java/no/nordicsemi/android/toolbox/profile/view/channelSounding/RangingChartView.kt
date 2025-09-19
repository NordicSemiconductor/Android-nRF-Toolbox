package no.nordicsemi.android.toolbox.profile.view.channelSounding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.nordicsemi.android.ui.view.createLinearTransition
import java.util.Locale

@Composable
internal fun RangingChartView(measurement: Float) {
    val duration = 1000
    val isInAccessibilityMode = rememberSaveable { mutableStateOf(false) }
    val transition = createLinearTransition(isInAccessibilityMode.value, duration)

    val rangeMax =
        if (measurement < 5) 5
        else if (measurement < 10) 10
        else if (measurement < 20) 20
        else if (measurement < 50) 50
        else if (measurement < 100) 100
        else if (measurement < 200) 200
        else if (measurement < 500) 500
        else 1000


    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val chartWidth = maxWidth
        val min = 0
        val max = rangeMax
        val diff = max - min
        val step = diff / 4f

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Chart itself
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
                        onClick = {},
                        onLongClick = { isInAccessibilityMode.value = !isInAccessibilityMode.value }
                    )
            ) {
                // background bar
                drawRoundRect(
                    color = transition.inactiveColor.value,
                    size = Size(chartWidth.toPx(), transition.height.value.toPx()),
                    cornerRadius = CornerRadius(
                        transition.radius.value.toPx(),
                        transition.radius.value.toPx()
                    )
                )

                // progress bar
                val progressWidth = when {
                    measurement <= min -> 0f
                    measurement >= max -> 1f
                    else -> (measurement - min) / (max - min).toFloat()
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

            Spacer(Modifier.height(4.dp))

            // Labels row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0..4) {
                    val labelValue = min + i * step
                    Text(
                        text = String.format(Locale.US, "%d m", labelValue.toInt()),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun RangingChartViewPreview() {
    RangingChartView(25f)
}