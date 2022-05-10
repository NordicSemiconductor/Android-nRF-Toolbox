package no.nordicsemi.android.uart.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.utils.EMPTY

@Composable
internal fun UARTAddConfigurationDialog(onEvent: (UARTViewEvent) -> Unit, onDismiss: () -> Unit) {
    val name = rememberSaveable { mutableStateOf(String.EMPTY) }
    val isError = rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.uart_configuration_dialog_title)) },
        text = { NameInput(name, isError) },
        confirmButton = {
            TextButton(onClick = {
                if (isNameValid(name.value)) {
                    onDismiss()
                    onEvent(OnAddConfiguration(name.value))
                } else {
                    isError.value = true
                }
            }) {
                Text(stringResource(id = R.string.uart_macro_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(id = R.string.uart_macro_dialog_dismiss))
            }
        }
    )
}

@Composable
private fun NameInput(
    name: MutableState<String>,
    isError: MutableState<Boolean>
) {
    Column {

        OutlinedTextField(
            value = name.value,
            label = { Text(stringResource(id = R.string.uart_configuration_hint)) },
            singleLine = true,
            onValueChange = {
                isError.value = false
                name.value = it
            }
        )

        val errorText = if (isError.value) {
            stringResource(id = R.string.uart_name_empty)
        } else {
            String.EMPTY
        }

        Text(
            text = errorText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

private fun isNameValid(name: String): Boolean {
    return name.isNotBlank()
}
