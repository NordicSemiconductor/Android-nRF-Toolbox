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

package no.nordicsemi.android.csc.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import no.nordicsemi.android.common.core.simpleSharedFlow
import no.nordicsemi.android.csc.data.CSCServiceData
import no.nordicsemi.android.csc.data.SpeedUnit
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.kotlin.ble.profile.csc.data.CSCData
import no.nordicsemi.android.kotlin.ble.profile.csc.data.WheelSize
import no.nordicsemi.android.kotlin.ble.profile.csc.data.WheelSizes
import no.nordicsemi.android.service.DisconnectAndStopEvent
import no.nordicsemi.android.service.OpenLoggerEvent
import no.nordicsemi.android.service.ServiceManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CSCRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val serviceManager: ServiceManager,
) {
    private val _wheelSize = MutableStateFlow(WheelSizes.default)
    internal val wheelSize = _wheelSize.asStateFlow()

    private val _data = MutableStateFlow(CSCServiceData())
    internal val data = _data.asStateFlow()

    private val _stopEvent = simpleSharedFlow<DisconnectAndStopEvent>()
    internal val stopEvent = _stopEvent.asSharedFlow()

    private val _loggerEvent = simpleSharedFlow<OpenLoggerEvent>()
    internal val loggerEvent = _loggerEvent.asSharedFlow()

    val isRunning = data.map { it.connectionState?.state == GattConnectionState.STATE_CONNECTED }

    fun launch(device: ServerDevice) {
        serviceManager.startService(CSCService::class.java, device)
    }

    fun onInitComplete(device: ServerDevice) {
        Log.d("AAATESTAAA", "onInitComplete: ${data.value}")
        if (_data.value.deviceName == null) {
            Log.d("AAATESTAAA", "AAA")
            _data.value = _data.value.copy(deviceName = device.name)
        }
    }

    internal fun setSpeedUnit(speedUnit: SpeedUnit) {
        _data.value = _data.value.copy(speedUnit = speedUnit)
    }

    fun setWheelSize(wheelSize: WheelSize) {
        _wheelSize.value = wheelSize
    }

    fun onConnectionStateChanged(connectionState: GattConnectionStateWithStatus?) {
        _data.value = _data.value.copy(connectionState = connectionState)
    }

    fun onBatteryLevelChanged(batteryLevel: Int) {
        _data.value = _data.value.copy(batteryLevel = batteryLevel)
    }

    fun onCSCDataChanged(cscData: CSCData) {
        _data.value = _data.value.copy(data = cscData)
    }

    fun openLogger() {
        _loggerEvent.tryEmit(OpenLoggerEvent())
    }

    fun release() {
        Log.d("AAATESTAAA", "release: ${data.value}")
        _data.value = CSCServiceData()
        _stopEvent.tryEmit(DisconnectAndStopEvent())
    }
}
