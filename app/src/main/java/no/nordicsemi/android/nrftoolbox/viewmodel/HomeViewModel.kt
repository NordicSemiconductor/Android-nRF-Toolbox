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

package no.nordicsemi.android.nrftoolbox.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.analytics.AppAnalytics
import no.nordicsemi.android.analytics.ProfileOpenEvent
import no.nordicsemi.android.cgms.repository.CGMRepository
import no.nordicsemi.android.common.logger.LoggerLauncher
import no.nordicsemi.android.common.navigation.DestinationId
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.csc.repository.CSCRepository
import no.nordicsemi.android.hrs.service.HRSRepository
import no.nordicsemi.android.hts.repository.HTSRepository
import no.nordicsemi.android.nrftoolbox.repository.ActivitySignals
import no.nordicsemi.android.nrftoolbox.view.HomeViewState
import no.nordicsemi.android.prx.repository.PRXRepository
import no.nordicsemi.android.rscs.repository.RSCSRepository
import no.nordicsemi.android.uart.repository.UARTRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val navigationManager: Navigator,
    activitySignals: ActivitySignals,
    cgmRepository: CGMRepository,
    cscRepository: CSCRepository,
    hrsRepository: HRSRepository,
    htsRepository: HTSRepository,
    prxRepository: PRXRepository,
    rscsRepository: RSCSRepository,
    uartRepository: UARTRepository,
    private val analytics: AppAnalytics
) : ViewModel() {

    private val _state = MutableStateFlow(HomeViewState())
    val state = _state.asStateFlow()

    init {
        cgmRepository.isRunning.onEach {
            _state.value = _state.value.copy(isCGMModuleRunning = it)
        }.launchIn(viewModelScope)

        cscRepository.isRunning.onEach {
            _state.value = _state.value.copy(isCSCModuleRunning = it)
        }.launchIn(viewModelScope)

        hrsRepository.isRunning.onEach {
            _state.value = _state.value.copy(isHRSModuleRunning = it)
        }.launchIn(viewModelScope)

        htsRepository.isRunning.onEach {
            _state.value = _state.value.copy(isHTSModuleRunning = it)
        }.launchIn(viewModelScope)

        prxRepository.isRunning.onEach {
            _state.value = _state.value.copy(isPRXModuleRunning = it)
        }.launchIn(viewModelScope)

        rscsRepository.isRunning.onEach {
            _state.value = _state.value.copy(isRSCSModuleRunning = it)
        }.launchIn(viewModelScope)

        uartRepository.isRunning.onEach {
            _state.value = _state.value.copy(isUARTModuleRunning = it)
        }.launchIn(viewModelScope)

        activitySignals.state.onEach {
            _state.value = _state.value.copyWithRefresh()
        }.launchIn(viewModelScope)
    }

    fun openProfile(destination: DestinationId<Unit, Unit>) {
        navigationManager.navigateTo(destination)
    }

    fun openLogger() {
        LoggerLauncher.launch(context, null)
    }

    fun logEvent(event: ProfileOpenEvent) {
        analytics.logEvent(event)
    }
}
