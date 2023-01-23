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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.theme.view.RadioButtonGroup
import no.nordicsemi.android.common.theme.view.RadioButtonItem
import no.nordicsemi.android.common.theme.view.RadioGroupViewEntity
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.data.MacroEol
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle
import no.nordicsemi.android.utils.EMPTY

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InputSection(onEvent: (UARTViewEvent) -> Unit) {
    val text = rememberSaveable { mutableStateOf(String.EMPTY) }
    val hint = stringResource(id = R.string.uart_input_hint)
    val checkedItem = rememberSaveable { mutableStateOf(MacroEol.values()[0]) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f)) {

            val scope = rememberCoroutineScope()
            val scrollState = rememberScrollState()

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 65.dp)
                    .verticalScroll(scrollState),
                value = text.value,
                label = { Text(hint) },
                onValueChange = { newValue: String ->
                    text.value = newValue
                    scope.launch {
                        scrollState.scrollTo(Int.MAX_VALUE)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.size(16.dp))

        Button(
            onClick = {
                onEvent(OnRunInput(text.value, checkedItem.value))
                text.value = String.EMPTY
            },
            modifier = Modifier.padding(top = 6.dp)
        ) {
            Text(text = stringResource(id = R.string.uart_send))
        }
    }
}

@Composable
internal fun EditInputSection(onEvent: (UARTViewEvent) -> Unit) {
    val checkedItem = rememberSaveable { mutableStateOf(MacroEol.values()[0]) }

    val items = MacroEol.values().map {
        RadioButtonItem(it.toDisplayString(), it == checkedItem.value)
    }
    val viewEntity = RadioGroupViewEntity(items)

    ScreenSection {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionTitle(
                resId = R.drawable.ic_input,
                title = stringResource(R.string.uart_input),
                menu = {
                    IconButton(onClick = { onEvent(MacroInputSwitchClick) }) {
                        Icon(
                            painterResource(id = R.drawable.ic_macro),
                            contentDescription = stringResource(id = R.string.uart_input_macro),
                        )
                    }
                }
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.uart_macro_dialog_eol),
                    style = MaterialTheme.typography.labelLarge
                )

                RadioButtonGroup(viewEntity) {
                    val i = items.indexOf(it)
                    checkedItem.value = MacroEol.values()[i]
                }
            }

            Spacer(modifier = Modifier.size(16.dp))
        }
    }
}
