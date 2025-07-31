package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.directionFinder.Range
import java.util.Locale

@Composable
internal fun RangeSlider(range: Range, onChange: (Range) -> Unit) {
    Column {
        RangeSliderView(range, onChange)
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.from),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = String.format(Locale.US, "%d", range.from),
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.to), style = MaterialTheme.typography.bodySmall)
                Text(
                    text = String.format(Locale.US, "%d", range.to),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable
fun RangeSliderView(
    range: Range,
    onChange: (Range) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..500f,
    step: Int = 1
) {
    val currentOnChange = rememberUpdatedState(onChange)
    val sliderValues = remember(range) { range.toFloatRange() }

    RangeSlider(
        modifier = Modifier.fillMaxWidth(),
        value = sliderValues,
        onValueChange = { newValues ->
            currentOnChange.value(
                Range(newValues.start.toInt(), newValues.endInclusive.toInt())
            )
        },
        valueRange = valueRange,
        steps = ((valueRange.endInclusive - valueRange.start) / step).toInt() - 1,
    )
}

private fun Range.toFloatRange(): ClosedFloatingPointRange<Float> =
    from.toFloat()..to.toFloat()

@Preview
@Composable
private fun RangeSliderViewPreview() {
    RangeSlider(Range(0, 50)) {}
}
