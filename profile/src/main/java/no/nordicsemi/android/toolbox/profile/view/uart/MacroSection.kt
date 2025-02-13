package no.nordicsemi.android.toolbox.profile.view.uart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.UARTViewState
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.UARTEvent
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun MacroSection(viewState: UARTViewState, onEvent: (DeviceConnectionViewEvent) -> Unit) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    if (showAddDialog) {
        UARTAddConfigurationDialog(onEvent) { showAddDialog = false }
    }

    if (showDeleteDialog) {
        DeleteConfigurationDialog(onEvent) { showDeleteDialog = false }
    }

    if (viewState.showEditDialog) {
        UARTAddMacroDialog(viewState.selectedMacro) { onEvent(it) }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScreenSection {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SectionTitle(
                    resId = R.drawable.ic_macro,
                    title = stringResource(R.string.uart_macros),
                    menu = {
                        viewState.selectedConfiguration?.let {
                            if (!viewState.isConfigurationEdited) {
                                IconButton(onClick = { onEvent(UARTEvent.OnEditConfiguration) }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        stringResource(id = R.string.uart_configuration_edit)
                                    )
                                }
                            } else {
                                IconButton(onClick = { onEvent(UARTEvent.OnEditConfiguration) }) {
                                    Icon(
                                        painterResource(id = R.drawable.ic_pencil_off),
                                        stringResource(id = R.string.uart_configuration_edit)
                                    )
                                }
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    stringResource(id = R.string.uart_configuration_delete)
                                )
                            }
                        }
                    }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        UARTConfigurationPicker(viewState, onEvent)
                    }

                    Button(onClick = { showAddDialog = true }) {
                        Text(stringResource(id = R.string.uart_configuration_add))
                    }
                }

                viewState.selectedConfiguration?.let {
                    UARTMacroView(it, viewState.isConfigurationEdited, onEvent)
                }
            }
        }
    }
}

@Preview
@Composable
private fun MacroSectionPreview() {
    MacroSection(UARTViewState()) {}
}

@Composable
private fun DeleteConfigurationDialog(
    onEvent: (DeviceConnectionViewEvent) -> Unit,
    onDismiss: () -> Unit
) {
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
            TextButton(onClick = {
                onDismiss()
                onEvent(UARTEvent.OnDeleteConfiguration)
            }) {
                Text(text = stringResource(id = R.string.uart_delete_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.uart_delete_dialog_cancel))
            }
        }
    )
}

@Preview
@Composable
private fun DeleteConfigurationDialogPreview() {
    DeleteConfigurationDialog({}, {})
}