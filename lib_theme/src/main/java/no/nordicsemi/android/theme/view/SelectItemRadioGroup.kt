package no.nordicsemi.android.theme.view

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
fun <T> SelectItemRadioGroup(
    currentItem: T,
    items: List<RadioGroupItem<T>>,
    onEvent: (RadioGroupItem<T>) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach {
            SelectItemRadioButton(currentItem, it, onEvent)
        }
    }
}

@Composable
internal fun <T> SelectItemRadioButton(
    selectedItem: T,
    displayedItem: RadioGroupItem<T>,
    onEvent: (RadioGroupItem<T>) -> Unit
) {
    Row {
        RadioButton(
            selected = (selectedItem == displayedItem.unit),
            onClick = { onEvent(displayedItem) }
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = displayedItem.label)
    }
}

data class RadioGroupItem<T>(val unit: T, val label: String)
