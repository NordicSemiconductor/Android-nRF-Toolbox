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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.theme.view.PagerView
import no.nordicsemi.android.common.theme.view.PagerViewEntity
import no.nordicsemi.android.common.theme.view.PagerViewItem
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.viewmodel.UARTViewModel
import no.nordicsemi.android.ui.view.NavigateUpButton
import no.nordicsemi.android.ui.view.ProfileAppBar
import no.nordicsemi.android.kotlin.ble.ui.scanner.view.DeviceConnectingView
import no.nordicsemi.android.kotlin.ble.ui.scanner.view.DeviceDisconnectedView

@Composable
fun UARTScreen() {
    val viewModel: UARTViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    val navigateUp = { viewModel.onEvent(NavigateUp) }

    Scaffold(
        topBar = {
            ProfileAppBar(
                deviceName = state.uartManagerState.deviceName,
                connectionState = state.uartManagerState.connectionState,
                title = R.string.uart_title,
                navigateUp = navigateUp,
                disconnect = { viewModel.onEvent(DisconnectEvent) },
                openLogger = { viewModel.onEvent(OpenLogger) }
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            when (state.uartManagerState.connectionState?.state) {
                null,
                GattConnectionState.STATE_CONNECTING -> PaddingBox { DeviceConnectingView { NavigateUpButton(navigateUp) } }
                GattConnectionState.STATE_DISCONNECTED,
                GattConnectionState.STATE_DISCONNECTING -> PaddingBox {
                    DeviceDisconnectedView(state.uartManagerState.disconnectStatus) { NavigateUpButton(navigateUp) }
                }
                GattConnectionState.STATE_CONNECTED -> SuccessScreen()
            }
        }
    }
}

@Composable
private fun PaddingBox(content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(16.dp)) {
        content()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SuccessScreen() {
    val input = stringResource(id = R.string.uart_input)
    val macros = stringResource(id = R.string.uart_macros)
    val viewEntity = remember {
        PagerViewEntity(
            listOf(
                PagerViewItem(input) { KeyboardView() },
                PagerViewItem(macros) { MacroView() }
            )
        )
    }
    PagerView(
        viewEntity = viewEntity,
        modifier = Modifier.fillMaxSize(),
        itemSpacing = 16.dp,
        coroutineScope = rememberCoroutineScope(),
        scrollable = false
    )
}

@Composable
private fun KeyboardView() {
    val viewModel: UARTViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value
    UARTContentView(state.uartManagerState) { viewModel.onEvent(it) }
}

@Composable
private fun MacroView() {
    val viewModel: UARTViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value
    MacroSection(state) { viewModel.onEvent(it) }
}
