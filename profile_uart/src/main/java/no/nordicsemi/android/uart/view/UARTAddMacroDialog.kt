package no.nordicsemi.android.uart.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import no.nordicsemi.android.material.you.HorizontalLabelRadioButtonGroup
import no.nordicsemi.android.material.you.RadioButtonItem
import no.nordicsemi.android.material.you.RadioGroupViewEntity
import no.nordicsemi.android.material.you.TextField
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.data.NewLineChar
import no.nordicsemi.android.uart.data.UARTMacro
import no.nordicsemi.android.utils.EMPTY

@Composable
internal fun UARTAddMacroDialog(onDismiss: () -> Unit, onEvent: (UARTViewEvent) -> Unit) {
    val command = remember { mutableStateOf(String.EMPTY) }
    val newLineChar = remember { mutableStateOf(NewLineChar.LF) }
    val isError = remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { onDismiss() }) {
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

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Column(modifier = Modifier.padding(bottom = 8.dp, start = 16.dp, end = 16.dp)) {

                        NewLineCharSection(newLineChar.value) { newLineChar.value = it }

                        Spacer(modifier = Modifier.size(16.dp))

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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { onDismiss() }) {
                                Text(stringResource(id = R.string.uart_macro_dialog_dismiss))
                            }

                            Spacer(modifier = Modifier.size(16.dp))

                            TextButton(onClick = {
                                if (isCommandValid(command.value)) {
                                    onDismiss()
                                    onEvent(OnCreateMacro(UARTMacro(command.value, newLineChar.value)))
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
private fun NewLineCharSection(checkedItem: NewLineChar, onItemClick: (NewLineChar) -> Unit) {
    val items = NewLineChar.values().map {
        RadioButtonItem(it.toDisplayString(), it == checkedItem)
    }
    val viewEntity = RadioGroupViewEntity(items)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(id = R.string.uart_macro_dialog_eol),
            style = MaterialTheme.typography.labelLarge
        )

        HorizontalLabelRadioButtonGroup(viewEntity) {
            val i = items.indexOf(it)
            onItemClick(NewLineChar.values()[i])
        }
    }
}

private fun isCommandValid(command: String): Boolean {
    return command.isNotBlank()
}
