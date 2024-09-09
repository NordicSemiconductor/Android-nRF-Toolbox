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

package no.nordicsemi.android.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.common.logger.view.LoggerAppBarIcon
import no.nordicsemi.android.common.ui.view.NordicAppBar
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloseIconAppBar(text: String, onClick: () -> Unit) {
    NordicAppBar(
        title = { Text(text) },
        backButtonIcon = Icons.Default.Close,
        onNavigationButtonClick = onClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggerIconAppBar(
    text: String,
    onClick: () -> Unit,
    isConnected: Boolean,
    onDisconnectClick: () -> Unit,
    onLoggerClick: () -> Unit
) {
    NordicAppBar(
        title = { Text(text) },
        onNavigationButtonClick = onClick,
        actions = {
            TextButton(
                onClick = onDisconnectClick,
                enabled = isConnected,
            ) {
                Text(stringResource(id = R.string.disconnect))
            }
            LoggerAppBarIcon(
                onClick = onLoggerClick
            )
        }
    )
}

@Composable
fun ProfileAppBar(
    deviceName: String?,
    connectionState: GattConnectionStateWithStatus?,
    navigateUp: () -> Unit,
    disconnect: () -> Unit,
    openLogger: () -> Unit
) {
    val isConnected = connectionState?.state == GattConnectionState.STATE_CONNECTED
    LoggerIconAppBar(deviceName ?: "No name", navigateUp, isConnected, disconnect, openLogger)
}
