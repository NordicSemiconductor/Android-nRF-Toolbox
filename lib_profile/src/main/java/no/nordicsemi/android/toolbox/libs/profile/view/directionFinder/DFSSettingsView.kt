package no.nordicsemi.android.toolbox.libs.profile.view.directionFinder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.core.data.Range
import no.nordicsemi.android.toolbox.libs.core.data.SensorData
import no.nordicsemi.android.toolbox.libs.core.data.availableSections
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DFSViewEvent
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.ui.view.SectionTitle
import java.util.Locale

@Composable
internal fun SettingsView(
    data: SensorData,
    onEvent: (DeviceConnectionViewEvent) -> Unit
) {
    val isExpanded = remember { mutableStateOf(false) }
    val range = Range(0, 50)

    SectionTitle(
        resId = R.drawable.ic_settings,
        title = stringResource(id = R.string.distance_settings),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded.value = !isExpanded.value },
        rotateArrow = if (isExpanded.value) 180f else 0f
    )

    AnimatedVisibility(
        visible = isExpanded.value,
        enter = expandIn(
            expandFrom = Alignment.Center,
            initialSize = { IntSize(it.width, 0) }
        ),
        exit = shrinkOut(
            shrinkTowards = Alignment.Center,
            targetSize = { IntSize(it.width, 0) })
    ) {
        Column {
            Spacer(modifier = Modifier.padding(8.dp))

            Text(
                stringResource(R.string.distance_range),
                style = MaterialTheme.typography.titleSmall
            )
            // TODO: add distance range.
            RangeSlider(range) {
                onEvent(DFSViewEvent.OnRangeChangedEvent(it))
            }

            Spacer(modifier = Modifier.padding(8.dp))

            if (data.availableSections().isNotEmpty()) {
                Text(
                    stringResource(R.string.measurement_details),
                    style = MaterialTheme.typography.titleSmall
                )

                MeasurementDetailModeView(data, onEvent)
            }
        }
    }
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
    viewEntity: SensorData,
    onEvent: (DeviceConnectionViewEvent) -> Unit
) {

    var width by rememberSaveable { mutableIntStateOf(0) }
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.clickable { isExpanded = true }) {
        // TODO: Add the name of measurement section when it is selected from ui. Grab it in some state and use it here.
        val text = stringResource(R.string.select_section)

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
                text = text,
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
                viewEntity.availableSections().forEachIndexed { index, it ->
                    Text(
                        text = it.displayName,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .clickable {
                                onEvent(DFSViewEvent.OnDetailsSectionParamsSelected(it))
                                isExpanded = false
                            }
                    )

                    if (index != viewEntity.availableSections().size - 1) {
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
