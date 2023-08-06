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

package no.nordicsemi.android.rscs.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.ui.scanner.view.DeviceConnectingView
import no.nordicsemi.android.kotlin.ble.ui.scanner.view.DeviceDisconnectedView
import no.nordicsemi.android.rscs.R
import no.nordicsemi.android.rscs.viewmodel.RSCSViewModel
import no.nordicsemi.android.ui.view.NavigateUpButton
import no.nordicsemi.android.ui.view.ProfileAppBar

@Composable
fun RSCSScreen() {
    val viewModel: RSCSViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    val navigateUp = { viewModel.onEvent(NavigateUpEvent) }

    Scaffold(
        topBar = {
            ProfileAppBar(
                deviceName = state.deviceName,
                connectionState = state.connectionState,
                title = R.string.rscs_title,
                navigateUp = navigateUp,
                disconnect = { viewModel.onEvent(DisconnectEvent) },
                openLogger = { viewModel.onEvent(OpenLoggerEvent) }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (state.connectionState?.state) {
                null,
                GattConnectionState.STATE_CONNECTING -> DeviceConnectingView { NavigateUpButton(navigateUp) }
                GattConnectionState.STATE_DISCONNECTED,
                GattConnectionState.STATE_DISCONNECTING -> DeviceDisconnectedView(state.disconnectStatus) {
                    NavigateUpButton(navigateUp)
                }
                GattConnectionState.STATE_CONNECTED -> RSCSContentView(state) { viewModel.onEvent(it) }
            }
        }
    }
}
