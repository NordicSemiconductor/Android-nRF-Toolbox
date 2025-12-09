package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.R
import java.util.Locale

@Composable
internal fun RangeSlider(range: IntRange, onChange: (IntRange) -> Unit) {
    Column {
        RangeSliderView(range, onChange)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.from),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${range.first}",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 8.dp),
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.to),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${range.last}",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
fun RangeSliderView(
    range: IntRange,
    onChange: (IntRange) -> Unit,
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
                IntRange(newValues.start.toInt(), newValues.endInclusive.toInt())
            )
        },
        valueRange = valueRange,
        steps = ((valueRange.endInclusive - valueRange.start) / step).toInt() - 1,
    )
}

private fun IntRange.toFloatRange(): ClosedFloatingPointRange<Float> =
    start.toFloat()..endInclusive.toFloat()

@Preview(showBackground = true)
@Composable
private fun RangeSliderViewPreview() {
    RangeSlider(
        range = 0..50,
        onChange = {},
    )
}
