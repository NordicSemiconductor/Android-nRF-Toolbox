package no.nordicsemi.android.uart.view

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import no.nordicsemi.android.material.you.TextField
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.utils.EMPTY

@Composable
internal fun UARTAddConfigurationDialog(onEvent: (UARTViewEvent) -> Unit, onDismiss: () -> Unit) {
    val name = rememberSaveable { mutableStateOf(String.EMPTY) }
    val isError = rememberSaveable { mutableStateOf(false) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(10.dp),
            shadowElevation = 2.dp,
        ) {
            Column(verticalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = stringResource(id = R.string.uart_configuration_dialog_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

//                Spacer(modifier = Modifier.height(16.dp))

                NameInput(name, isError)

//                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text(stringResource(id = R.string.uart_macro_dialog_dismiss))
                    }

                    Spacer(modifier = Modifier.size(16.dp))

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
                }
            }
        }
    }
}

@Composable
private fun NameInput(
    name: MutableState<String>,
    isError: MutableState<Boolean>
) {
    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            text = name.value,
            hint = stringResource(id = R.string.uart_configuration_hint)
        ) {
            isError.value = false
            name.value = it
        }

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

        Spacer(modifier = Modifier.size(16.dp))
    }
}

private fun isNameValid(name: String): Boolean {
    return name.isNotBlank()
}
