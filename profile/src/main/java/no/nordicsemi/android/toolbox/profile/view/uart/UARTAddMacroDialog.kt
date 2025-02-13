package no.nordicsemi.android.toolbox.profile.view.uart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.ui.view.RadioButtonGroup
import no.nordicsemi.android.common.ui.view.RadioButtonItem
import no.nordicsemi.android.common.ui.view.RadioGroupViewEntity
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.uart.MacroEol
import no.nordicsemi.android.toolbox.profile.data.uart.MacroIcon
import no.nordicsemi.android.toolbox.profile.data.uart.UARTMacro
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.UARTEvent

private const val GRID_SIZE = 5

@Composable
internal fun UARTAddMacroDialog(macro: UARTMacro?, onEvent: (DeviceConnectionViewEvent) -> Unit) {
    val newLineChar = rememberSaveable { mutableStateOf(macro?.newLineChar ?: MacroEol.LF) }
    val command = rememberSaveable { mutableStateOf(macro?.command ?:"") }
    val selectedIcon = rememberSaveable { mutableStateOf(macro?.icon ?: MacroIcon.entries.toTypedArray()[0]) }

    AlertDialog(
        onDismissRequest = { onEvent(UARTEvent.OnEditFinished) },
        dismissButton = {
            TextButton(onClick = { onEvent(UARTEvent.OnDeleteMacro) }) {
                Text(stringResource(id = R.string.uart_macro_dialog_delete))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onEvent(
                    UARTEvent.OnCreateMacro(
                        UARTMacro(
                            selectedIcon.value,
                            command.value,
                            newLineChar.value
                        )
                    )
                )
            }) {
                Text(stringResource(id = R.string.uart_macro_dialog_confirm))
            }
        },
        title = {
            Text(
                text = stringResource(id = R.string.uart_macro_dialog_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(GRID_SIZE),
                modifier = Modifier.wrapContentHeight()
            ) {
                item(span = { GridItemSpan(GRID_SIZE) }) {
                    Column {
                        NewLineCharSection(newLineChar.value) { newLineChar.value = it }

                        Spacer(modifier = Modifier.size(16.dp))
                    }
                }

                item(span = { GridItemSpan(GRID_SIZE) }) {
                    CommandInput(command)
                }

                items(20) { item ->
                    val icon = MacroIcon.create(item)
                    val background = if (selectedIcon.value == icon) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        Color.Transparent
                    }

                    Image(
                        painter = painterResource(id = icon.index),
                        contentDescription = stringResource(id = R.string.uart_macro_icon),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedIcon.value = icon }
                            .background(background)
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun UARTAddMacroDialogPreview() {
    UARTAddMacroDialog(null) {}
}

@Composable
private fun CommandInput(command: MutableState<String>) {
    Column {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = command.value,
            label = { Text(stringResource(id = R.string.uart_macro_dialog_command)) },
            onValueChange = {
                command.value = it
            }
        )

        Spacer(modifier = Modifier.size(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun CommandInputPreview() {
    val command = rememberSaveable { mutableStateOf("AT+") }
    CommandInput(command)
}

@Composable
private fun NewLineCharSection(checkedItem: MacroEol, onItemClick: (MacroEol) -> Unit) {
    val items = MacroEol.entries.map {
        RadioButtonItem(it.toString(), it == checkedItem)
    }
    val viewEntity = RadioGroupViewEntity(items)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(id = R.string.uart_macro_dialog_eol),
            style = MaterialTheme.typography.labelLarge
        )

        RadioButtonGroup(viewEntity) {
            val i = items.indexOf(it)
            onItemClick(MacroEol.entries[i])
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NewLineCharSectionPreview() {
    val newLineChar = rememberSaveable { mutableStateOf(MacroEol.LF) }
    NewLineCharSection(newLineChar.value) {}
}
