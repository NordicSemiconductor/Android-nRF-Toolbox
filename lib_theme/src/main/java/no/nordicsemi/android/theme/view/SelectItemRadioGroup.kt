package no.nordicsemi.android.theme.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import no.nordicsemi.android.material.you.RadioButton
import no.nordicsemi.android.material.you.RadioButtonItem

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
            RadioButtonItem(displayedItem.label, selectedItem == displayedItem.unit),
        ) {
            onEvent(displayedItem)
        }
    }
}

data class RadioGroupItem<T>(val unit: T, val label: String)
