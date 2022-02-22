package no.nordicsemi.android.uart.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.material.you.TextField
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.utils.EMPTY

@Composable
internal fun UARTAddConfigurationDialog(onEvent: (UARTViewEvent) -> Unit) {
    val name = remember { mutableStateOf(String.EMPTY) }
    val isError = remember { mutableStateOf(false) }

    Column {
        NameInput(name, isError)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { onEvent(OnEditFinish) }) {
                Text(stringResource(id = R.string.uart_macro_dialog_dismiss))
            }

            Spacer(modifier = Modifier.size(16.dp))

            TextButton(onClick = {
                if (isNameValid(name.value)) {
                    onEvent(OnEditFinish)
                    onEvent(OnAddConfiguration(name.value))
                } else {
                    isError.value = true
                }
            }) {
                Text(stringResource(id = R.string.uart_macro_dialog_confirm))
            }
        }
    }

}

@Composable
private fun NameInput(
    name: MutableState<String>,
    isError: MutableState<Boolean>
) {
    Column {
        TextField(
            text = name.value,
            hint = stringResource(id = R.string.uart_macro_dialog_command)
        ) {
            isError.value = false
            name.value = it
        }

        if (isError.value) {
            Text(
                text = stringResource(id = R.string.uart_name_empty),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.size(16.dp))
    }
}

private fun isNameValid(name: String): Boolean {
    return name.isNotBlank()
}
