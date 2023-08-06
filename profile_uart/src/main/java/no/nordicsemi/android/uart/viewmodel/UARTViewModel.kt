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

package no.nordicsemi.android.uart.viewmodel

import android.os.ParcelUuid
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.analytics.AppAnalytics
import no.nordicsemi.android.analytics.Profile
import no.nordicsemi.android.analytics.ProfileConnectedEvent
import no.nordicsemi.android.analytics.UARTChangeConfiguration
import no.nordicsemi.android.analytics.UARTCreateConfiguration
import no.nordicsemi.android.analytics.UARTMode
import no.nordicsemi.android.analytics.UARTSendAnalyticsEvent
import no.nordicsemi.android.common.navigation.NavigationResult
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.toolbox.scanner.ScannerDestinationId
import no.nordicsemi.android.uart.data.MacroEol
import no.nordicsemi.android.uart.data.UARTConfiguration
import no.nordicsemi.android.uart.data.UARTMacro
import no.nordicsemi.android.uart.data.UARTPersistentDataSource
import no.nordicsemi.android.uart.repository.UARTRepository
import no.nordicsemi.android.uart.repository.UART_SERVICE_UUID
import no.nordicsemi.android.uart.view.ClearOutputItems
import no.nordicsemi.android.uart.view.DisconnectEvent
import no.nordicsemi.android.uart.view.MacroInputSwitchClick
import no.nordicsemi.android.uart.view.NavigateUp
import no.nordicsemi.android.uart.view.OnAddConfiguration
import no.nordicsemi.android.uart.view.OnConfigurationSelected
import no.nordicsemi.android.uart.view.OnCreateMacro
import no.nordicsemi.android.uart.view.OnDeleteConfiguration
import no.nordicsemi.android.uart.view.OnDeleteMacro
import no.nordicsemi.android.uart.view.OnEditConfiguration
import no.nordicsemi.android.uart.view.OnEditFinish
import no.nordicsemi.android.uart.view.OnEditMacro
import no.nordicsemi.android.uart.view.OnRunInput
import no.nordicsemi.android.uart.view.OnRunMacro
import no.nordicsemi.android.uart.view.OpenLogger
import no.nordicsemi.android.uart.view.UARTViewEvent
import no.nordicsemi.android.uart.view.UARTViewState
import no.nordicsemi.android.ui.view.NordicLoggerFactory
import javax.inject.Inject

@HiltViewModel
internal class UARTViewModel @Inject constructor(
    private val repository: UARTRepository,
    private val navigationManager: Navigator,
    private val dataSource: UARTPersistentDataSource,
    private val analytics: AppAnalytics,
    private val loggerFactory: NordicLoggerFactory
) : ViewModel() {

    private val _state = MutableStateFlow(UARTViewState())
    val state = _state.asStateFlow()

    init {
        repository.setOnScreen(true)

        viewModelScope.launch {
            if (repository.isRunning.firstOrNull() == false) {
                requestBluetoothDevice()
            }
        }

        repository.data.onEach {
            _state.value = _state.value.copy(uartManagerState = it)

            if (it.connectionState?.state == GattConnectionState.STATE_CONNECTED) {
                analytics.logEvent(ProfileConnectedEvent(Profile.UART))
            }
        }.launchIn(viewModelScope)

        dataSource.getConfigurations().onEach {
            _state.value = _state.value.copy(configurations = it)
        }.launchIn(viewModelScope)

        repository.lastConfigurationName.onEach {
            it?.let {
                _state.value = _state.value.copy(selectedConfigurationName = it)
            }
        }.launchIn(viewModelScope)
    }

    private fun requestBluetoothDevice() {
        navigationManager.navigateTo(ScannerDestinationId, ParcelUuid(UART_SERVICE_UUID))

        navigationManager.resultFrom(ScannerDestinationId)
            .onEach { handleResult(it) }
            .launchIn(viewModelScope)
    }

    internal fun handleResult(result: NavigationResult<ServerDevice>) {
        when (result) {
            is NavigationResult.Cancelled -> navigationManager.navigateUp()
            is NavigationResult.Success -> onDeviceSelected(result.value)
        }
    }

    private fun onDeviceSelected(device: ServerDevice) {
        repository.launch(device)
    }

    fun onEvent(event: UARTViewEvent) {
        when (event) {
            is OnCreateMacro -> addNewMacro(event.macro)
            OnDeleteMacro -> deleteMacro()
            DisconnectEvent -> disconnect()
            is OnRunMacro -> runMacro(event.macro)
            NavigateUp -> navigationManager.navigateUp()
            is OnEditMacro -> onEditMacro(event)
            OnEditFinish -> onEditFinish()
            is OnConfigurationSelected -> onConfigurationSelected(event)
            is OnAddConfiguration -> onAddConfiguration(event)
            OnDeleteConfiguration -> deleteConfiguration()
            OnEditConfiguration -> onEditConfiguration()
            ClearOutputItems -> repository.clearItems()
            OpenLogger -> repository.openLogger()
            is OnRunInput -> sendText(event.text, event.newLineChar)
            MacroInputSwitchClick -> onMacroInputSwitch()
        }
    }

    private fun runMacro(macro: UARTMacro) {
        repository.runMacro(macro)
        analytics.logEvent(UARTSendAnalyticsEvent(UARTMode.MACRO))
    }

    private fun sendText(text: String, newLineChar: MacroEol) {
        repository.sendText(text, newLineChar)
        analytics.logEvent(UARTSendAnalyticsEvent(UARTMode.TEXT))
    }

    private fun onMacroInputSwitch() {
        _state.value = _state.value.copy(isInputVisible = !state.value.isInputVisible)
    }

    private fun onEditConfiguration() {
        val isEdited = _state.value.isConfigurationEdited
        _state.value = _state.value.copy(isConfigurationEdited = !isEdited)
    }

    private fun onAddConfiguration(event: OnAddConfiguration) {
        viewModelScope.launch(Dispatchers.IO) {
            dataSource.saveConfiguration(UARTConfiguration(null, event.name))
            _state.value = _state.value.copy(selectedConfigurationName = event.name)
        }
        saveLastConfigurationName(event.name)
        analytics.logEvent(UARTCreateConfiguration())
    }

    private fun onEditMacro(event: OnEditMacro) {
        _state.value = _state.value.copy(editedPosition = event.position)
    }

    private fun onEditFinish() {
        _state.value = _state.value.copy(editedPosition = null)
    }

    private fun onConfigurationSelected(event: OnConfigurationSelected) {
        saveLastConfigurationName(event.configuration.name)
        analytics.logEvent(UARTChangeConfiguration())
    }

    private fun saveLastConfigurationName(name: String) {
        viewModelScope.launch {
            repository.saveConfigurationName(name)
        }
    }

    private fun addNewMacro(macro: UARTMacro) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value.selectedConfiguration?.let {
                val macros = it.macros.toMutableList().apply {
                    set(_state.value.editedPosition!!, macro)
                }
                val newConf = it.copy(macros = macros)
                dataSource.saveConfiguration(newConf)
                _state.value = _state.value.copy(editedPosition = null)
            }
        }
    }

    private fun deleteConfiguration() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value.selectedConfiguration?.let {
                dataSource.deleteConfiguration(it)
            }
        }
    }

    private fun deleteMacro() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value.selectedConfiguration?.let {
                val macros = it.macros.toMutableList().apply {
                    set(_state.value.editedPosition!!, null)
                }
                val newConf = it.copy(macros = macros)
                dataSource.saveConfiguration(newConf)
                _state.value = _state.value.copy(editedPosition = null)
            }
        }
    }

    private fun disconnect() {
        repository.disconnect()
        navigationManager.navigateUp()
    }

    override fun onCleared() {
        super.onCleared()
        repository.setOnScreen(false)
    }
}
