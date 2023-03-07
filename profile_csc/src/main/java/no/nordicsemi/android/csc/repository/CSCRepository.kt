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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import no.nordicsemi.android.common.logger.NordicLogger
import no.nordicsemi.android.csc.data.CSCServicesData
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.profile.csc.CSCData
import no.nordicsemi.android.kotlin.ble.profile.csc.WheelSize
import no.nordicsemi.android.kotlin.ble.profile.csc.WheelSizes
import no.nordicsemi.android.service.ServiceManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CSCRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val serviceManager: ServiceManager,
) {
    private var logger: NordicLogger? = null

    private val _wheelSize = MutableStateFlow(WheelSizes.default)
    internal val wheelSize = _wheelSize.asStateFlow()

    private val _data = MutableStateFlow(CSCServicesData())
    internal val data = _data.asStateFlow()

    val isRunning = data.map { it.connectionState == GattConnectionState.STATE_CONNECTED }

    fun launch(device: ServerDevice) {
        serviceManager.startService(CSCService::class.java, device)
    }

    fun setWheelSize(wheelSize: WheelSize) {
        _wheelSize.value = wheelSize
    }

    fun onConnectionStateChanged(connectionState: GattConnectionState) {
        _data.value = _data.value.copy(connectionState = connectionState)
    }

    fun onBatteryLevelChanged(batteryLevel: Int) {
        _data.value = _data.value.copy(batteryLevel = batteryLevel)
    }

    fun onCSCDataChanged(cscData: CSCData) {
        _data.value = _data.value.copy(data = cscData)
    }

    fun openLogger() {
        NordicLogger.launch(context, logger)
    }

    fun release() {
        logger = null
        serviceManager.stopService(CSCService::class.java)
    }
}
