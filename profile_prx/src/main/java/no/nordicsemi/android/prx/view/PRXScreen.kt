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

package no.nordicsemi.android.prx.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.ui.scanner.view.DeviceConnectingView
import no.nordicsemi.android.common.ui.scanner.view.DeviceDisconnectedView
import no.nordicsemi.android.common.ui.scanner.view.Reason
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.prx.R
import no.nordicsemi.android.prx.data.PRXServiceData
import no.nordicsemi.android.prx.viewmodel.PRXViewModel
import no.nordicsemi.android.ui.view.BackIconAppBar
import no.nordicsemi.android.ui.view.LoggerIconAppBar
import no.nordicsemi.android.ui.view.NavigateUpButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PRXScreen() {
    val viewModel: PRXViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    val navigateUp = { viewModel.onEvent(NavigateUpEvent) }

    Scaffold(
        topBar = { AppBar(state, navigateUp, viewModel) }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (state.connectionState?.state) {
                null,
                GattConnectionState.STATE_CONNECTING -> DeviceConnectingView { NavigateUpButton(navigateUp) }
                GattConnectionState.STATE_DISCONNECTED,
                GattConnectionState.STATE_DISCONNECTING -> DeviceDisconnectedView(state.connectionState.status) {
                    NavigateUpButton(navigateUp)
                }
                GattConnectionState.STATE_CONNECTED -> ContentView(state) { viewModel.onEvent(it) }
            }
        }
    }
}

private fun getReason(isLinkLoss: Boolean): Reason {
    return if (isLinkLoss) {
        Reason.LINK_LOSS
    } else {
        Reason.UNKNOWN
    }
}

@Composable
private fun AppBar(state: PRXServiceData, navigateUp: () -> Unit, viewModel: PRXViewModel) {
    if (state.deviceName?.isNotBlank() == true) {
        LoggerIconAppBar(state.deviceName, navigateUp, { viewModel.onEvent(DisconnectEvent) }) {
            viewModel.onEvent(OpenLoggerEvent)
        }
    } else {
        BackIconAppBar(stringResource(id = R.string.prx_title), navigateUp)
    }
}
