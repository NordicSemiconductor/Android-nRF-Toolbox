package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.SensorData
import no.nordicsemi.android.toolbox.profile.data.directionFinder.Range
import no.nordicsemi.android.toolbox.profile.data.directionFinder.availableSections
import no.nordicsemi.android.toolbox.profile.viewmodel.DFSEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.ProfileUiEvent
import no.nordicsemi.android.ui.view.SectionTitle
import java.util.Locale

@Composable
internal fun SettingsView(
    data: SensorData,
    range: Range,
    onEvent: (ProfileUiEvent) -> Unit
) {
    val isExpanded = remember { mutableStateOf(false) }

    SectionTitle(
        icon = Icons.Default.Settings,
        title = stringResource(id = R.string.distance_settings),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded.value = !isExpanded.value },
    )

    Column {
        if (data.availableSections().isNotEmpty()) {
            Text(
                stringResource(R.string.measurement_details),
                style = MaterialTheme.typography.titleSmall
            )

            MeasurementDetailModeView(data, onEvent)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsViewPreview() {
    SettingsView(
        SensorData(),
        Range(0, 50)
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
    onEvent: (ProfileUiEvent) -> Unit
) {
    var width by rememberSaveable { mutableIntStateOf(0) }
    var isExpanded by remember { mutableStateOf(false) }
    var displayText by rememberSaveable { mutableStateOf("") }

    Column(modifier = Modifier.clickable { isExpanded = true }) {
        val text = sensorData.selectedMeasurementSection?.displayName
            ?: stringResource(R.string.select_section)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(
                    BorderStroke(2.dp, MaterialTheme.colorScheme.onBackground),
                    shape = CircleShape
                )
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayText.ifEmpty { text },
                modifier = Modifier
                    .onSizeChanged { width = it.width }
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = "")
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier.width(with(LocalDensity.current) { width.toDp() }),
        ) {
            Column {
                sensorData.availableSections().forEachIndexed { index, it ->
                    Text(
                        text = it.displayName,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .clickable {
                                onEvent(DFSEvent.OnDetailsSectionParamsSelected(it))
                                displayText = it.displayName
                                isExpanded = false
                            }
                    )

                    if (index != sensorData.availableSections().size - 1) {
                        Spacer(modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MeasurementDetailModeViewPreview() {
    MeasurementDetailModeView(
        SensorData()
    ) {}
}
