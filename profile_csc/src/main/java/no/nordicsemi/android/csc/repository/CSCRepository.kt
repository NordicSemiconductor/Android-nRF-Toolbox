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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.common.logger.NordicLogger
import no.nordicsemi.android.common.logger.NordicLoggerFactory
import no.nordicsemi.android.common.ui.scanner.model.DiscoveredBluetoothDevice
import no.nordicsemi.android.csc.data.CSCData
import no.nordicsemi.android.csc.data.CSCManager
import no.nordicsemi.android.csc.data.WheelSize
import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.android.service.IdleResult
import no.nordicsemi.android.service.ServiceManager
import no.nordicsemi.android.ui.view.StringConst
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CSCRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val serviceManager: ServiceManager,
    private val loggerFactory: NordicLoggerFactory,
    private val stringConst: StringConst
) {
    private var manager: CSCManager? = null
    private var logger: NordicLogger? = null

    private val _data = MutableStateFlow<BleManagerResult<CSCData>>(IdleResult())
    internal val data = _data.asStateFlow()

    val isRunning = data.map { it.isRunning() }
    val hasBeenDisconnected = data.map { it.hasBeenDisconnected() }

    fun launch(device: DiscoveredBluetoothDevice) {
        serviceManager.startService(CSCService::class.java, device)
    }

    fun start(device: DiscoveredBluetoothDevice, scope: CoroutineScope) {
        val createdLogger = loggerFactory.create(stringConst.APP_NAME, "CSC", device.address).also {
            logger = it
        }
        val manager = CSCManager(context, scope, createdLogger)
        this.manager = manager

        manager.dataHolder.status.onEach {
            _data.value = it
        }.launchIn(scope)

        scope.launch {
            manager.start(device)
        }
    }

    fun setWheelSize(wheelSize: WheelSize) {
        manager?.setWheelSize(wheelSize)
    }

    private suspend fun CSCManager.start(device: DiscoveredBluetoothDevice) {
        try {
            connect(device.device)
                .useAutoConnect(false)
                .retry(3, 100)
                .suspend()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openLogger() {
        NordicLogger.launch(context, logger)
    }

    fun release() {
        manager?.disconnect()?.enqueue()
        logger = null
        manager = null
    }
}
