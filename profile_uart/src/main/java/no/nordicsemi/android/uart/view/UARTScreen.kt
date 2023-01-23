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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.theme.view.PagerView
import no.nordicsemi.android.common.theme.view.PagerViewEntity
import no.nordicsemi.android.common.theme.view.PagerViewItem
import no.nordicsemi.android.common.ui.scanner.view.DeviceConnectingView
import no.nordicsemi.android.common.ui.scanner.view.DeviceDisconnectedView
import no.nordicsemi.android.common.ui.scanner.view.Reason
import no.nordicsemi.android.service.ConnectedResult
import no.nordicsemi.android.service.ConnectingResult
import no.nordicsemi.android.service.DeviceHolder
import no.nordicsemi.android.service.DisconnectedResult
import no.nordicsemi.android.service.IdleResult
import no.nordicsemi.android.service.LinkLossResult
import no.nordicsemi.android.service.MissingServiceResult
import no.nordicsemi.android.service.SuccessResult
import no.nordicsemi.android.service.UnknownErrorResult
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.data.UARTData
import no.nordicsemi.android.uart.viewmodel.UARTViewModel
import no.nordicsemi.android.ui.view.BackIconAppBar
import no.nordicsemi.android.ui.view.LoggerIconAppBar
import no.nordicsemi.android.ui.view.NavigateUpButton

@Composable
fun UARTScreen() {
    val viewModel: UARTViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    Column {
        val navigateUp = { viewModel.onEvent(NavigateUp) }

        AppBar(state = state, navigateUp = navigateUp) { viewModel.onEvent(it) }

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            when (state.uartManagerState) {
                NoDeviceState -> DeviceConnectingView()
                is WorkingState -> when (state.uartManagerState.result) {
                    is IdleResult,
                    is ConnectingResult -> DeviceConnectingView { NavigateUpButton(navigateUp) }
                    is ConnectedResult -> DeviceConnectingView { NavigateUpButton(navigateUp) }
                    is DisconnectedResult -> DeviceDisconnectedView(Reason.USER) { NavigateUpButton(navigateUp) }
                    is LinkLossResult -> DeviceDisconnectedView(Reason.LINK_LOSS) { NavigateUpButton(navigateUp) }
                    is MissingServiceResult -> DeviceDisconnectedView(Reason.MISSING_SERVICE) { NavigateUpButton(navigateUp) }
                    is UnknownErrorResult -> DeviceDisconnectedView(Reason.UNKNOWN) { NavigateUpButton(navigateUp) }
                    is SuccessResult -> SuccessScreen(state.uartManagerState.result.data, state, viewModel)
                }
            }
        }
    }
}

@Composable
private fun AppBar(state: UARTViewState, navigateUp: () -> Unit, onEvent: (UARTViewEvent) -> Unit) {
    val toolbarName = (state.uartManagerState as? WorkingState)?.let {
        (it.result as? DeviceHolder)?.deviceName()
    }

    if (toolbarName == null) {
        BackIconAppBar(stringResource(id = R.string.uart_title), navigateUp)
    } else {
        LoggerIconAppBar(toolbarName, navigateUp, { onEvent(DisconnectEvent) }) {
            onEvent(OpenLogger)
        }
    }
}

@Composable
private fun SuccessScreen(data: UARTData, state: UARTViewState, viewModel: UARTViewModel) {
    val viewEntity = PagerViewEntity(
        listOf(
            PagerViewItem(stringResource(id = R.string.uart_input)) {
                UARTContentView(data) { viewModel.onEvent(it) }
            },
            PagerViewItem(stringResource(id = R.string.uart_macros)) {
                MacroSection(state) { viewModel.onEvent(it) }
            }
        )
    )
    PagerView(viewEntity)
}

@Composable
fun Scroll(content: @Composable () -> Unit) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        content()
    }
}
