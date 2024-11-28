package no.nordicsemi.android.toolbox.libs.profile.view.directionFinder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.ui.view.createAngularTransition

@Composable
internal fun DataSmoothingView(data: List<Int>) {
    Text(
        text = stringResource(id = R.string.value_smoothing_info),
        style = MaterialTheme.typography.bodyMedium
    )

    Spacer(modifier = Modifier.padding(8.dp))

    Column(horizontalAlignment = Alignment.Start) {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DataSmoothingChart(
    modifier: Modifier = Modifier,
    data: List<Int>?
) {
    if (data?.isEmpty() == true) return

    val totalRecords = data?.size ?: 1
    val averageValue = if (data?.isNotEmpty() == true) data.sorted()[data.size / 2] else 0
    val maxValue = data?.maxOrNull()!!

    val duration = 1000
    val isInAccessibilityMode = rememberSaveable { mutableStateOf(false) }
    val transition = createAngularTransition(isInAccessibilityMode.value, duration)

    Canvas(
        modifier = modifier
            .height(transition.height.value)
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
                color = transition.chartColor.value,
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
            color = transition.avgLineColor.value,
            strokeWidth = transition.avgLineWidth.value.toPx()
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
