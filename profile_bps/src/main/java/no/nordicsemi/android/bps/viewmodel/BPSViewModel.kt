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

package no.nordicsemi.android.bps.viewmodel

import android.os.ParcelUuid
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.analytics.AppAnalytics
import no.nordicsemi.android.analytics.Profile
import no.nordicsemi.android.analytics.ProfileConnectedEvent
import no.nordicsemi.android.bps.data.BPS_SERVICE_UUID
import no.nordicsemi.android.bps.repository.BPSRepository
import no.nordicsemi.android.bps.view.BPSViewEvent
import no.nordicsemi.android.bps.view.BPSViewState
import no.nordicsemi.android.bps.view.DisconnectEvent
import no.nordicsemi.android.bps.view.NoDeviceState
import no.nordicsemi.android.bps.view.OpenLoggerEvent
import no.nordicsemi.android.bps.view.WorkingState
import no.nordicsemi.android.common.navigation.NavigationResult
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.ui.scanner.model.DiscoveredBluetoothDevice
import no.nordicsemi.android.service.ConnectedResult
import no.nordicsemi.android.toolbox.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class BPSViewModel @Inject constructor(
    private val repository: BPSRepository,
    private val navigationManager: Navigator,
    private val analytics: AppAnalytics
) : ViewModel() {

    private val _state = MutableStateFlow<BPSViewState>(NoDeviceState)
    val state = _state.asStateFlow()

    init {
        navigationManager.navigateTo(ScannerDestinationId, ParcelUuid(BPS_SERVICE_UUID))

        navigationManager.resultFrom(ScannerDestinationId)
            .onEach { handleArgs(it) }
            .launchIn(viewModelScope)
    }

    private fun handleArgs(result: NavigationResult<DiscoveredBluetoothDevice>) {
        when (result) {
            is NavigationResult.Cancelled -> navigationManager.navigateUp()
            is NavigationResult.Success -> connectDevice(result.value)
        }
    }

    fun onEvent(event: BPSViewEvent) {
        when (event) {
            DisconnectEvent -> navigationManager.navigateUp()
            OpenLoggerEvent -> repository.openLogger()
        }
    }

    private fun connectDevice(device: DiscoveredBluetoothDevice) {
        repository.downloadData(viewModelScope, device).onEach {
            _state.value = WorkingState(it)

            (it as? ConnectedResult)?.let {
                analytics.logEvent(ProfileConnectedEvent(Profile.BPS))
            }
        }.launchIn(viewModelScope)
    }
}
