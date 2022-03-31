package no.nordicsemi.android.uart.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.GridItemSpan
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import no.nordicsemi.android.material.you.RadioButtonGroup
import no.nordicsemi.android.material.you.RadioButtonItem
import no.nordicsemi.android.material.you.RadioGroupViewEntity
import no.nordicsemi.android.material.you.TextField
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.data.MacroEol
import no.nordicsemi.android.uart.data.MacroIcon
import no.nordicsemi.android.uart.data.UARTMacro
import no.nordicsemi.android.utils.EMPTY

private const val GRID_SIZE = 5

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun UARTAddMacroDialog(macro: UARTMacro?, onEvent: (UARTViewEvent) -> Unit) {
    val newLineChar = rememberSaveable { mutableStateOf(macro?.newLineChar ?: MacroEol.LF) }
    val command = rememberSaveable { mutableStateOf(macro?.command ?: String.EMPTY) }
    val isError = rememberSaveable { mutableStateOf(false) }
    val selectedIcon = rememberSaveable { mutableStateOf(macro?.icon ?: MacroIcon.values()[0]) }

    Dialog(onDismissRequest = { onEvent(OnEditFinish) }) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(10.dp),
            shadowElevation = 0.dp,
        ) {
            Column {
                Text(
                    text = stringResource(id = R.string.uart_macro_dialog_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                LazyVerticalGrid(
                    cells = GridCells.Fixed(GRID_SIZE),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .wrapContentHeight()
                ) {
                    item(span = { GridItemSpan(GRID_SIZE) }) {
                        Column {
                            NewLineCharSection(newLineChar.value) { newLineChar.value = it }

                            Spacer(modifier = Modifier.size(16.dp))
                        }
                    }

                    item(span = { GridItemSpan(GRID_SIZE) }) {
                        CommandInput(command, isError)
                    }

                    items(20) { item ->
                        val icon = MacroIcon.create(item)
                        val background = if (selectedIcon.value == icon) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        }

                        Image(
                            painter = painterResource(id = icon.toResId()),
                            contentDescription = stringResource(id = R.string.uart_macro_icon),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedIcon.value = icon }
                                .background(background)
                        )
                    }

                    item(span = { GridItemSpan(GRID_SIZE) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { onEvent(OnEditFinish) }) {
                                Text(stringResource(id = R.string.uart_macro_dialog_dismiss))
                            }

                            Spacer(modifier = Modifier.size(16.dp))

                            TextButton(onClick = { onEvent(OnDeleteMacro) }) {
                                Text(stringResource(id = R.string.uart_macro_dialog_delete))
                            }

                            Spacer(modifier = Modifier.size(16.dp))

                            TextButton(onClick = {
                                if (isCommandValid(command.value)) {
                                    onEvent(
                                        OnCreateMacro(
                                            UARTMacro(
                                                selectedIcon.value,
                                                command.value,
                                                newLineChar.value
                                            )
                                        )
                                    )
                                } else {
                                    isError.value = true
                                }
                            }) {
                                Text(stringResource(id = R.string.uart_macro_dialog_confirm))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommandInput(
    command: MutableState<String>,
    isError: MutableState<Boolean>
) {
    Column {
        TextField(
            text = command.value,
            hint = stringResource(id = R.string.uart_macro_dialog_command)
        ) {
            isError.value = false
            command.value = it
        }

        if (isError.value) {
            Text(
                text = stringResource(id = R.string.uart_macro_error),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun NewLineCharSection(checkedItem: MacroEol, onItemClick: (MacroEol) -> Unit) {
    val items = MacroEol.values().map {
        RadioButtonItem(it.toDisplayString(), it == checkedItem)
    }
    val viewEntity = RadioGroupViewEntity(items)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(id = R.string.uart_macro_dialog_eol),
            style = MaterialTheme.typography.labelLarge
        )

        RadioButtonGroup(viewEntity) {
            val i = items.indexOf(it)
            onItemClick(MacroEol.values()[i])
        }
    }
}

private fun isCommandValid(command: String): Boolean {
    return command.isNotBlank()
}
