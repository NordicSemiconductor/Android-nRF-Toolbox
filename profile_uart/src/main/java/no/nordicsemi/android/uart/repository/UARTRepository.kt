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

package no.nordicsemi.android.uart.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import no.nordicsemi.android.common.core.simpleSharedFlow
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.service.DisconnectAndStopEvent
import no.nordicsemi.android.service.OpenLoggerEvent
import no.nordicsemi.android.service.ServiceManager
import no.nordicsemi.android.uart.data.ConfigurationDataSource
import no.nordicsemi.android.uart.data.MacroEol
import no.nordicsemi.android.uart.data.UARTMacro
import no.nordicsemi.android.uart.data.UARTRecord
import no.nordicsemi.android.uart.data.UARTRecordType
import no.nordicsemi.android.uart.data.UARTServiceData
import no.nordicsemi.android.uart.data.parseWithNewLineChar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UARTRepository @Inject internal constructor(
    @ApplicationContext
    private val context: Context,
    private val serviceManager: ServiceManager,
    private val configurationDataSource: ConfigurationDataSource
) {
    private val _data = MutableStateFlow(UARTServiceData())
    internal val data = _data.asStateFlow()

    private val _stopEvent = simpleSharedFlow<DisconnectAndStopEvent>()
    internal val stopEvent = _stopEvent.asSharedFlow()

    private val _command = simpleSharedFlow<String>()
    internal val command = _command.asSharedFlow()

    private val _loggerEvent = simpleSharedFlow<OpenLoggerEvent>()
    internal val loggerEvent = _loggerEvent.asSharedFlow()

    val isRunning = data.map { it.connectionState?.state == GattConnectionState.STATE_CONNECTED }

    val lastConfigurationName = configurationDataSource.lastConfigurationName

    fun launch(device: ServerDevice) {
        serviceManager.startService(UARTService::class.java, device)
    }

    fun onConnectionStateChanged(connectionState: GattConnectionStateWithStatus?) {
        _data.value = _data.value.copy(connectionState = connectionState)
    }

    fun onBatteryLevelChanged(batteryLevel: Int) {
        _data.value = _data.value.copy(batteryLevel = batteryLevel)
    }

    fun onNewMessageReceived(value: String) {
        _data.value = _data.value.copy(messages = _data.value.messages + UARTRecord(value, UARTRecordType.OUTPUT))
    }

    fun onNewMessageSent(value: String) {
        _data.value = _data.value.copy(messages = _data.value.messages + UARTRecord(value, UARTRecordType.INPUT))
    }

    fun onInitComplete(device: ServerDevice) {
        _data.value = _data.value.copy(deviceName = device.name)
    }

    fun sendText(text: String, newLineChar: MacroEol) {
        _command.tryEmit(text.parseWithNewLineChar(newLineChar))
    }

    fun runMacro(macro: UARTMacro) {
        if (macro.command == null) {
            return
        }
        _command.tryEmit(macro.command.parseWithNewLineChar(macro.newLineChar))
    }

    fun clearItems() {
        _data.value = _data.value.copy(messages = emptyList())
    }

    fun openLogger() {
        _loggerEvent.tryEmit(OpenLoggerEvent())
    }

    suspend fun saveConfigurationName(name: String) {
        configurationDataSource.saveConfigurationName(name)
    }

    fun stop() {
        _stopEvent.tryEmit(DisconnectAndStopEvent())
    }

    fun clear() {
        _data.value = UARTServiceData()
    }
}
