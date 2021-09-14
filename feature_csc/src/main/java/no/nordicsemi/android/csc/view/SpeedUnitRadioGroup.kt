package no.nordicsemi.android.csc.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun SpeedUnitRadioGroup(
    currentUnit: SpeedUnit,
    onEvent: (OnSelectedSpeedUnitSelected) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SpeedUnitRadioButton(currentUnit, SpeedUnit.KM_H, onEvent)
        SpeedUnitRadioButton(currentUnit, SpeedUnit.MPH, onEvent)
        SpeedUnitRadioButton(currentUnit, SpeedUnit.M_S, onEvent)
    }
}

@Composable
internal fun SpeedUnitRadioButton(
    selectedUnit: SpeedUnit,
    displayedUnit: SpeedUnit,
    onEvent: (OnSelectedSpeedUnitSelected) -> Unit
) {
    Row {
        RadioButton(
            selected = (selectedUnit == displayedUnit),
            onClick = { onEvent(OnSelectedSpeedUnitSelected(displayedUnit)) }
        )
        Text(text = createSpeedUnitLabel(displayedUnit))
    }
}

internal fun createSpeedUnitLabel(unit: SpeedUnit): String {
    return when (unit) {
        SpeedUnit.M_S -> "m/s"
        SpeedUnit.KM_H -> "km/h"
        SpeedUnit.MPH -> "mph"
    }
}
