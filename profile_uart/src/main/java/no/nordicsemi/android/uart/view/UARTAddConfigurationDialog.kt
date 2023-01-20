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

@OptIn(ExperimentalMaterial3Api::class)
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
