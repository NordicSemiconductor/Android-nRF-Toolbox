package no.nordicsemi.android.toolbox.profile.view.uart

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.UARTViewState
import no.nordicsemi.android.toolbox.profile.viewmodel.ProfileUiEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.UARTEvent
import no.nordicsemi.android.ui.view.TextInputField

@Composable
internal fun UARTAddConfigurationDialog(
    viewState: UARTViewState,
    onEvent: (ProfileUiEvent) -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var isError by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.uart_configuration_dialog_title)) },
        text = {
            TextInputField(
                input = name,
                label = stringResource(id = R.string.uart_configuration_hint),
                placeholder = "Enter configuration name",
                errorMessage = stringResource(id = R.string.uart_name_empty),
                errorState = isError,
            ) {
                name = it
                isError = !isNameValid(it)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (isNameValid(name) && viewState.isNameUnique(name)) {
                    onDismiss()
                    onEvent(UARTEvent.OnAddConfiguration(name.trim()))
                } else {
                    isError = true
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

/**
 * Check if the name is unique.
 * A name is unique if it does not exist in the list of configurations.
 *
 * @param name The name to check.
 * @return True if the name is unique, false otherwise.
 */
private fun UARTViewState.isNameUnique(name: String): Boolean {
    return configurations.none { it.name == name.trim() }
}

/**
 * Check if the name is valid.
 * A name is valid if it is not empty or blank.
 *
 * @param name The name to check.
 * @return True if the name is valid, false otherwise.
 */
private fun isNameValid(name: String): Boolean = name.trim().isNotBlank()
