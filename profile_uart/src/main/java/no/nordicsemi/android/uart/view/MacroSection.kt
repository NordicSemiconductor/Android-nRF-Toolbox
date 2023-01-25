/*
 * Copyright (c) 2022, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.uart.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

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

    if (viewState.showEditDialog) {
        UARTAddMacroDialog(viewState.selectedMacro) { onEvent(it) }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .heightIn(min = 400.dp)
    ) {
        ScreenSection {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SectionTitle(
                    resId = R.drawable.ic_macro,
                    title = stringResource(R.string.uart_macros),
                    menu = {
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
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Box(modifier = Modifier.weight(1f)) {
                        UARTConfigurationPicker(viewState, onEvent)
                    }

                    Spacer(modifier = Modifier.size(16.dp))

                    Button(onClick = { showAddDialog.value = true }) {
                        Text(stringResource(id = R.string.uart_configuration_add))
                    }
                }

                viewState.selectedConfiguration?.let {
                    Spacer(modifier = Modifier.height(16.dp))

                    UARTMacroView(it, viewState.isConfigurationEdited, onEvent)
                }
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
            TextButton(onClick = {
                onDismiss()
                onEvent(OnDeleteConfiguration)
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
