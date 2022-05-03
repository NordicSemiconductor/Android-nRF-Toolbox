package no.nordicsemi.android.uart.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.material.you.ScreenSection
import no.nordicsemi.android.theme.view.SectionTitle
import no.nordicsemi.android.uart.R

@Composable
internal fun MacroSection(viewState: UARTViewState, onEvent: (UARTViewEvent) -> Unit) {
    val showAddDialog = rememberSaveable { mutableStateOf(false) }
    val showDeleteDialog = rememberSaveable { mutableStateOf(false) }

    if (showAddDialog.value) {
        UARTAddConfigurationDialog(onEvent) { showAddDialog.value = false }
    }

    if (showDeleteDialog.value) {
        DeleteConfigurationDialog(onEvent) { showDeleteDialog.value = false }
    }

    ScreenSection {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionTitle(
                resId = R.drawable.ic_macro,
                title = stringResource(R.string.uart_macros),
                menu = {
                    IconButton(onClick = { onEvent(MacroInputSwitchClick) }) {
                        Icon(
                            painterResource(id = R.drawable.ic_input),
                            contentDescription = stringResource(id = R.string.uart_input_macro),
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Box(modifier = Modifier.weight(1f)) {
                    UARTConfigurationPicker(viewState, onEvent)
                }

                IconButton(onClick = { showAddDialog.value = true }) {
                    Icon(Icons.Default.Add, stringResource(id = R.string.uart_configuration_add))
                }

                viewState.selectedConfiguration?.let {

                    if (!viewState.isConfigurationEdited) {
                        IconButton(onClick = { onEvent(OnEditConfiguration) }) {
                            Icon(
                                Icons.Default.Edit,
                                stringResource(id = R.string.uart_configuration_edit)
                            )
                        }
                    } else {
                        IconButton(onClick = { onEvent(OnEditConfiguration) }) {
                            Icon(
                                painterResource(id = R.drawable.ic_pencil_off),
                                stringResource(id = R.string.uart_configuration_edit)
                            )
                        }
                    }

                    IconButton(onClick = { showDeleteDialog.value = true }) {
                        Icon(
                            Icons.Default.Delete,
                            stringResource(id = R.string.uart_configuration_delete)
                        )
                    }
                }
            }

            viewState.selectedConfiguration?.let {
                Spacer(modifier = Modifier.height(16.dp))

                UARTMacroView(it, viewState.isConfigurationEdited, onEvent)
            }
        }
    }
}

@Composable
private fun DeleteConfigurationDialog(onEvent: (UARTViewEvent) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.uart_delete_dialog_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(text = stringResource(id = R.string.uart_delete_dialog_info))
        },
        confirmButton = {
            Button(onClick = {
                onDismiss()
                onEvent(OnDeleteConfiguration)
            }) {
                Text(text = stringResource(id = R.string.uart_delete_dialog_confirm))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.uart_delete_dialog_cancel))
            }
        }
    )
}
