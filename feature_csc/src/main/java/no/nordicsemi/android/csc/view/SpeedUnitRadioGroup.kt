package no.nordicsemi.android.csc.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun SpeedUnitRadioGroup(
    currentItem: RadioGroupItem,
    items: List<RadioGroupItem>,
    onEvent: (RadioGroupItem) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach {
            SpeedUnitRadioButton(currentItem, it, onEvent)
        }
    }
}

@Composable
internal fun SpeedUnitRadioButton(
    selectedItem: RadioGroupItem,
    displayedItem: RadioGroupItem,
    onEvent: (RadioGroupItem) -> Unit
) {
    Row {
        RadioButton(
            selected = (selectedItem == displayedItem),
            onClick = { onEvent(displayedItem) }
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = displayedItem.label)
    }
}

internal fun createSpeedUnitLabel(unit: SpeedUnit): String {
    return when (unit) {
        SpeedUnit.M_S -> "m/s"
        SpeedUnit.KM_H -> "km/h"
        SpeedUnit.MPH -> "mph"
    }
}

data class RadioGroupItem(val label: String)
