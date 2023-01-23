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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.data.UARTConfiguration
import no.nordicsemi.android.ui.view.dialog.FlowCanceled
import no.nordicsemi.android.ui.view.dialog.ItemSelectedResult
import no.nordicsemi.android.ui.view.dialog.StringListDialog
import no.nordicsemi.android.ui.view.dialog.StringListDialogConfig
import no.nordicsemi.android.ui.view.dialog.StringListDialogResult
import no.nordicsemi.android.ui.view.dialog.toAnnotatedString

@Composable
internal fun UARTConfigurationPicker(state: UARTViewState, onEvent: (UARTViewEvent) -> Unit) {
    val showDialog = rememberSaveable { mutableStateOf(false) }

    UARTConfigurationButton(state.selectedConfiguration) {
        showDialog.value = true
    }

    if (showDialog.value) {
        SelectWheelSizeDialog(state) {
            when (it) {
                FlowCanceled -> showDialog.value = false
                is ItemSelectedResult -> {
                    onEvent(OnConfigurationSelected(state.configurations[it.index]))
                    showDialog.value = false
                }
            }
        }
    }
}

@Composable
internal fun SelectWheelSizeDialog(state: UARTViewState, onEvent: (StringListDialogResult) -> Unit) {
    val wheelEntries = state.configurations.map { it.name }

    StringListDialog(createConfig(wheelEntries) {
        onEvent(it)
    })
}

@Composable
private fun createConfig(entries: List<String>, onResult: (StringListDialogResult) -> Unit): StringListDialogConfig {
    return StringListDialogConfig(
        title = stringResource(id = R.string.uart_configuration_picker_dialog).toAnnotatedString(),
        items = entries,
        onResult = onResult,
        leftIcon = R.drawable.ic_uart_settings
    )
}

@Composable
internal fun UARTConfigurationButton(configuration: UARTConfiguration?, onClick: () -> Unit) {
    OutlinedButton(onClick = { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Column {
                Text(
                    text = stringResource(id = R.string.uart_configuration_picker_hint),
                    style = MaterialTheme.typography.labelSmall
                )
                val text = configuration?.name ?: stringResource(id = R.string.uart_configuration_picker_not_selected)
                Text(text = text, style = MaterialTheme.typography.bodyMedium)
            }

            Icon(Icons.Default.ArrowDropDown, contentDescription = "")
        }
    }
}
