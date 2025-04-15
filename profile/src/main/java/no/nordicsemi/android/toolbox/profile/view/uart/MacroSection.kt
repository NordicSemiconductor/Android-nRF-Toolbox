package no.nordicsemi.android.toolbox.profile.view.uart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.UARTViewState
import no.nordicsemi.android.toolbox.profile.data.uart.UARTConfiguration
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.UARTEvent
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun MacroSection(
    isMacroFocused: MutableState<Boolean>,
    viewState: UARTViewState = UARTViewState(),
    onEvent: (DeviceConnectionViewEvent) -> Unit
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    if (showAddDialog) {
        UARTAddConfigurationDialog(viewState, onEvent) { showAddDialog = false }
    }

    if (showDeleteDialog) {
        viewState.selectedConfiguration?.let {
            DeleteConfigurationDialog(it, onEvent) { showDeleteDialog = false }
        }
    }

    if (viewState.showEditDialog) {
        UARTAddMacroDialog(viewState.selectedMacro) { onEvent(it) }
    }

    Column {
        OutlinedCard(
            border = if (isMacroFocused.value) BorderStroke(
                width = 2.dp, color = MaterialTheme.colorScheme.primary
            ) else CardDefaults.outlinedCardBorder(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (viewState.configurations.isEmpty()) {
                    SectionTitle(
                        resId = R.drawable.ic_macro,
                        title = stringResource(id = R.string.uart_macros),
                        menu = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(id = R.string.uart_configuration_add),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { showAddDialog = true }
                                    .padding(8.dp)
                            )
                        }
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Box(modifier = Modifier.weight(1f)) {
                            UARTConfigurationPicker(viewState, onEvent)
                        }

                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(id = R.string.uart_configuration_add),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { showAddDialog = true }
                                .padding(8.dp)
                        )

                        viewState.selectedConfiguration?.let {
                            if (!viewState.isConfigurationEdited) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(id = R.string.uart_configuration_edit),
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { onEvent(UARTEvent.OnEditConfiguration) }
                                        .padding(8.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.EditOff,
                                    contentDescription = stringResource(id = R.string.uart_configuration_edit),
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { onEvent(UARTEvent.OnEditConfiguration) }
                                        .padding(8.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(id = R.string.uart_configuration_delete),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { showDeleteDialog = true }
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                viewState.selectedConfiguration?.let {
                    UARTMacroView(it, viewState.isConfigurationEdited, onEvent)
                }
            }
        }
    }
}

@Composable
private fun DeleteConfigurationDialog(
    selectedConfiguration: UARTConfiguration,
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
                onEvent(UARTEvent.OnDeleteConfiguration(selectedConfiguration))
                onDismiss()
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
    DeleteConfigurationDialog(UARTConfiguration(null, "Config 1"), {}, {})
}