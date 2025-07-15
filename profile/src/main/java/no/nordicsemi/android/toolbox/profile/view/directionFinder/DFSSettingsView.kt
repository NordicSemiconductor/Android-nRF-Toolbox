package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.SensorData
import no.nordicsemi.android.toolbox.profile.data.directionFinder.MeasurementSection
import no.nordicsemi.android.toolbox.profile.data.directionFinder.Range
import no.nordicsemi.android.toolbox.profile.data.directionFinder.availableSections
import no.nordicsemi.android.toolbox.profile.viewmodel.DFSEvent
import no.nordicsemi.android.ui.view.DropdownView
import no.nordicsemi.android.ui.view.SectionTitle
import java.util.Locale

@Composable
internal fun SettingsView(
    data: SensorData,
    onEvent: (DFSEvent) -> Unit
) {
    SectionTitle(
        icon = Icons.Default.Settings,
        title = stringResource(id = R.string.distance_settings),
        modifier = Modifier.fillMaxWidth(),
    )

    if (data.availableSections().isNotEmpty()) {
        MeasurementDetailModeView(data, onEvent)
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsViewPreview() {
    SettingsView(
        SensorData()
    ) {}
}

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


@Composable
internal fun MeasurementDetailModeView(
    sensorData: SensorData,
    onEvent: (DFSEvent) -> Unit
) {
    var displayText by rememberSaveable { mutableStateOf("") }
    val text = sensorData.selectedMeasurementSection?.displayName
        ?: MeasurementSection.DISTANCE_MCPD_BEST.displayName

    DropdownView(
        items = sensorData.availableSections(),
        label = text,
        placeholder = displayText.ifEmpty { text },
        onItemSelected = {
            onEvent(DFSEvent.OnDetailsSectionParamsSelected(it))
            displayText = it.displayName
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun MeasurementDetailModeViewPreview() {
    MeasurementDetailModeView(
        SensorData()
    ) {}
}
