package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.R

@Composable
internal fun DataSmoothingView(data: List<Int>) {
    Text(
        text = stringResource(id = R.string.value_smoothing_info),
        style = MaterialTheme.typography.bodyMedium
    )

    Spacer(modifier = Modifier.padding(8.dp))

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "${data.maxOrNull()}",
            style = MaterialTheme.typography.labelSmall
        )

        Spacer(modifier = Modifier.padding(2.dp))
        DataSmoothingChart(data = data)
        Spacer(modifier = Modifier.padding(2.dp))

        Text(
            text = "0",
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
internal fun DataSmoothingChart(
    modifier: Modifier = Modifier,
    data: List<Int>
) {
    if (data.isEmpty()) return

    val totalRecords = data.size
    val averageValue = if (data.isNotEmpty()) {
        data.sorted()[data.size / 2]
    } else {
        0
    }
    val maxValue = data.maxOrNull()!!

    val duration = 1000
    val isInAccessibilityMode = rememberSaveable { mutableStateOf(false) }
    val transition = updateTransition(
        targetState = isInAccessibilityMode,
        label = "Accessibility transition"
    )
    val chartColor = transition.animateColor(
        label = "",
        transitionSpec = { TweenSpec(duration, 0, LinearOutSlowInEasing) }
    ) {
        if (it.value) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    }.value
    val avgLineColor = transition.animateColor(
        label = "",
        transitionSpec = { TweenSpec(duration, 0, LinearOutSlowInEasing) }
    ) {
        if (it.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
    }.value
    val avgLineWidth = transition.animateDp(
        label = "",
        transitionSpec = { TweenSpec(duration, 0, LinearOutSlowInEasing) }
    ) {
        if (it.value) 8.dp else 2.dp
    }.value
    val height = transition.animateDp(
        label = "",
        transitionSpec = { TweenSpec(duration, 0, LinearOutSlowInEasing) }
    ) {
        if (it.value) 100.dp else 50.dp
    }.value

    Canvas(
        modifier = modifier
            .height(height)
            .fillMaxWidth()
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                },
                onLongClick = { isInAccessibilityMode.value = !isInAccessibilityMode.value }
            )
    ) {

        val lineDistance = size.width / totalRecords

        val cHeight = size.height

        data.forEachIndexed { index, transactionRate ->
            val offsetX = lineDistance * index
            val offsetY = calculateYOffset(
                higherTransactionRateValue = maxValue,
                currentTransactionRate = transactionRate,
                canvasHeight = cHeight
            )
            val sizeY = calculateHeight(
                higherTransactionRateValue = maxValue,
                currentTransactionRate = transactionRate,
                canvasHeight = cHeight
            )
            drawRect(
                color = chartColor,
                topLeft = Offset(
                    x = offsetX,
                    y = offsetY
                ),
                size = Size(
                    width = lineDistance,
                    height = sizeY
                )
            )
        }
        drawLine(
            start = Offset(
                x = 0f,
                y = calculateYOffset(
                    higherTransactionRateValue = maxValue,
                    currentTransactionRate = averageValue,
                    canvasHeight = cHeight
                )
            ),
            end = Offset(
                x = size.width,
                y = calculateYOffset(
                    higherTransactionRateValue = maxValue,
                    currentTransactionRate = averageValue,
                    canvasHeight = cHeight
                )
            ),
            color = avgLineColor,
            strokeWidth = avgLineWidth.toPx()
        )
    }
}

private fun calculateYOffset(
    higherTransactionRateValue: Int,
    currentTransactionRate: Int,
    canvasHeight: Float
): Float {
    val maxAndCurrentValueDifference = (higherTransactionRateValue - currentTransactionRate)
    val relativePercentageOfScreen = (canvasHeight / higherTransactionRateValue)
    return maxAndCurrentValueDifference * relativePercentageOfScreen
}

private fun calculateHeight(
    higherTransactionRateValue: Int,
    currentTransactionRate: Int,
    canvasHeight: Float
): Float {
    val relativePercentageOfScreen = (canvasHeight / higherTransactionRateValue)
    return currentTransactionRate * relativePercentageOfScreen
}

@Preview(showBackground = true)
@Composable
private fun DataSmoothingViewPreview() {
    DataSmoothingView(
        data = listOf(10, 20, 30, 40, 50, 60, 70, 80, 90, 100)
    )
}