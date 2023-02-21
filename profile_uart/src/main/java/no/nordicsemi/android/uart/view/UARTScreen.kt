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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.pager.ExperimentalPagerApi
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
import no.nordicsemi.android.uart.viewmodel.UARTViewModel
import no.nordicsemi.android.ui.view.BackIconAppBar
import no.nordicsemi.android.ui.view.LoggerIconAppBar
import no.nordicsemi.android.ui.view.NavigateUpButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UARTScreen() {
    val viewModel: UARTViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val navigateUp = { viewModel.onEvent(NavigateUp) }

    Scaffold(
        topBar = { AppBar(state, navigateUp) { viewModel.onEvent(it) } }
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            when (val uartState = state.uartManagerState) {
                NoDeviceState -> PaddingBox { DeviceConnectingView() }
                is WorkingState -> when (uartState.result) {
                    is IdleResult,
                    is ConnectingResult -> PaddingBox { DeviceConnectingView { NavigateUpButton(navigateUp) } }
                    is ConnectedResult -> PaddingBox { DeviceConnectingView { NavigateUpButton(navigateUp) } }
                    is DisconnectedResult -> PaddingBox { DeviceDisconnectedView(Reason.USER) { NavigateUpButton(navigateUp) } }
                    is LinkLossResult -> PaddingBox { DeviceDisconnectedView(Reason.LINK_LOSS) { NavigateUpButton(navigateUp) } }
                    is MissingServiceResult -> PaddingBox { DeviceDisconnectedView(Reason.MISSING_SERVICE) { NavigateUpButton(navigateUp) } }
                    is UnknownErrorResult -> PaddingBox { DeviceDisconnectedView(Reason.UNKNOWN) { NavigateUpButton(navigateUp) } }
                    is SuccessResult -> SuccessScreen()
                }
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

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun SuccessScreen() {
    val input = stringResource(id = R.string.uart_input)
    val macros = stringResource(id = R.string.uart_macros)
    val viewEntity = remember { PagerViewEntity(
        listOf(
            PagerViewItem(input) { KeyboardView() },
            PagerViewItem(macros) { MacroView() }
        )
    ) }
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
    val state by viewModel.state.collectAsStateWithLifecycle()
    (state.uartManagerState as? WorkingState)?.let {
        (it.result as? SuccessResult)?.let {
            UARTContentView(it.data) { viewModel.onEvent(it) }
        }
    }
}

@Composable
private fun MacroView() {
    val viewModel: UARTViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    (state.uartManagerState as? WorkingState)?.let {
        (it.result as? SuccessResult)?.let {
            MacroSection(state) { viewModel.onEvent(it) }
        }
    }
}

@Composable
fun Scroll(content: @Composable () -> Unit) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        content()
    }
}
