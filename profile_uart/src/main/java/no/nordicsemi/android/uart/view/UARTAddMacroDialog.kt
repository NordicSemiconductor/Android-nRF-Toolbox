package no.nordicsemi.android.uart.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.material.you.TextField
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.data.UARTMacro
import no.nordicsemi.android.utils.EMPTY

@Composable
internal fun UARTAddMacroDialog(onDismiss: () -> Unit, onEvent: (UARTViewEvent) -> Unit) {
    val alias = remember { mutableStateOf(String.EMPTY) }
    val command = remember { mutableStateOf(String.EMPTY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(id = R.string.uart_macro_dialog_title))
        },
        text = {
            Column {
                TextField(text = alias.value, hint = stringResource(id = R.string.uart_macro_dialog_alias)) {
                    alias.value = it
                }

                Spacer(modifier = Modifier.size(16.dp))

                TextField(text = command.value, hint = stringResource(id = R.string.uart_macro_dialog_command)) {
                    command.value = it
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onEvent(OnCreateMacro(UARTMacro(alias.value, command.value)))
                }
            ) {
                Text(stringResource(id = R.string.uart_macro_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text(stringResource(id = R.string.uart_macro_dialog_dismiss))
            }
        }
    )
}
