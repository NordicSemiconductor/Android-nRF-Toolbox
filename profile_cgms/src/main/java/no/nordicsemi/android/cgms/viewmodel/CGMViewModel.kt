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

package no.nordicsemi.android.cgms.viewmodel

import android.os.ParcelUuid
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.analytics.AppAnalytics
import no.nordicsemi.android.analytics.Profile
import no.nordicsemi.android.analytics.ProfileConnectedEvent
import no.nordicsemi.android.cgms.data.CGMS_SERVICE_UUID
import no.nordicsemi.android.cgms.data.CGMServiceCommand
import no.nordicsemi.android.cgms.repository.CGMRepository
import no.nordicsemi.android.cgms.view.CGMViewEvent
import no.nordicsemi.android.cgms.view.CGMViewState
import no.nordicsemi.android.cgms.view.DisconnectEvent
import no.nordicsemi.android.cgms.view.NavigateUp
import no.nordicsemi.android.cgms.view.NoDeviceState
import no.nordicsemi.android.cgms.view.OnWorkingModeSelected
import no.nordicsemi.android.cgms.view.OpenLoggerEvent
import no.nordicsemi.android.cgms.view.WorkingState
import no.nordicsemi.android.common.navigation.NavigationResult
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.ui.scanner.model.DiscoveredBluetoothDevice
import no.nordicsemi.android.service.ConnectedResult
import no.nordicsemi.android.toolbox.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class CGMViewModel @Inject constructor(
    private val repository: CGMRepository,
    private val navigationManager: Navigator,
    private val analytics: AppAnalytics
) : ViewModel() {

    private val _state = MutableStateFlow<CGMViewState>(NoDeviceState)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (repository.isRunning.firstOrNull() == false) {
                requestBluetoothDevice()
            }
        }

        repository.data.onEach {
            _state.value = WorkingState(it)

            (it as? ConnectedResult)?.let {
                analytics.logEvent(ProfileConnectedEvent(Profile.CGMS))
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: CGMViewEvent) {
        when (event) {
            DisconnectEvent -> disconnect()
            is OnWorkingModeSelected -> onCommandReceived(event.workingMode)
            NavigateUp -> navigationManager.navigateUp()
            OpenLoggerEvent -> repository.openLogger()
        }
    }

    private fun requestBluetoothDevice() {
        navigationManager.navigateTo(ScannerDestinationId, ParcelUuid(CGMS_SERVICE_UUID))

        navigationManager.resultFrom(ScannerDestinationId)
            .onEach { handleResult(it) }
            .launchIn(viewModelScope)
    }

    private fun handleResult(result: NavigationResult<DiscoveredBluetoothDevice>) {
        when (result) {
            is NavigationResult.Cancelled -> navigationManager.navigateUp()
            is NavigationResult.Success -> repository.launch(result.value)
        }
    }

    private fun onCommandReceived(workingMode: CGMServiceCommand) {
        when (workingMode) {
            CGMServiceCommand.REQUEST_ALL_RECORDS -> repository.requestAllRecords()
            CGMServiceCommand.REQUEST_LAST_RECORD -> repository.requestLastRecord()
            CGMServiceCommand.REQUEST_FIRST_RECORD -> repository.requestFirstRecord()
            CGMServiceCommand.DISCONNECT -> disconnect()
        }
    }

    private fun disconnect() {
        repository.release()
        navigationManager.navigateUp()
    }
}
