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

package no.nordicsemi.android.prx.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.common.logger.NordicLogger
import no.nordicsemi.android.common.logger.NordicLoggerFactory
import no.nordicsemi.android.common.ui.scanner.model.DiscoveredBluetoothDevice
import no.nordicsemi.android.prx.data.AlarmLevel
import no.nordicsemi.android.prx.data.PRXData
import no.nordicsemi.android.prx.data.PRXManager
import no.nordicsemi.android.prx.data.ProximityServerManager
import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.android.service.IdleResult
import no.nordicsemi.android.service.LinkLossResult
import no.nordicsemi.android.service.ServiceManager
import no.nordicsemi.android.service.SuccessResult
import no.nordicsemi.android.ui.view.StringConst
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PRXRepository @Inject internal constructor(
    @ApplicationContext
    private val context: Context,
    private val serviceManager: ServiceManager,
    private val proximityServerManager: ProximityServerManager,
    private val alarmHandler: AlarmHandler,
    private val loggerFactory: NordicLoggerFactory,
    private val stringConst: StringConst
) {

    private var manager: PRXManager? = null
    private var logger: NordicLogger? = null

    private val _data = MutableStateFlow<BleManagerResult<PRXData>>(IdleResult())
    internal val data = _data.asStateFlow()

    val isRunning = data.map { it.isRunning() }
    val hasBeenDisconnectedWithoutLinkLoss = data.map { it.hasBeenDisconnectedWithoutLinkLoss() }

    fun launch(device: DiscoveredBluetoothDevice) {
        serviceManager.startService(PRXService::class.java, device)
        proximityServerManager.open()
    }

    fun start(device: DiscoveredBluetoothDevice, scope: CoroutineScope) {
        val createdLogger = loggerFactory.create(stringConst.APP_NAME, "PRX", device.address).also {
            logger = it
        }
        val manager = PRXManager(context, scope, createdLogger)
        this.manager = manager
        manager.useServer(proximityServerManager)

        manager.dataHolder.status.onEach {
            _data.value = it
            handleLocalAlarm(it)
        }.launchIn(scope)

        manager.connect(device.device)
            .useAutoConnect(true)
            .retry(3, 100)
            .enqueue()
    }

    private fun handleLocalAlarm(result: BleManagerResult<PRXData>) {
        (result as? SuccessResult<PRXData>)?.let {
            if (it.data.localAlarmLevel != AlarmLevel.NONE) {
                alarmHandler.playAlarm(it.data.localAlarmLevel)
            } else {
                alarmHandler.pauseAlarm()
            }
        }
        (result as? LinkLossResult<PRXData>)?.let {
            val alarmLevel = it.data?.linkLossAlarmLevel ?: AlarmLevel.HIGH
            alarmHandler.playAlarm(alarmLevel)
        }
    }

    fun enableAlarm() {
        manager?.writeImmediateAlert(true)
    }

    fun disableAlarm() {
        manager?.writeImmediateAlert(false)
    }

    fun openLogger() {
        NordicLogger.launch(context, logger)
    }

    fun release() {
        disableAlarm()
        manager?.disconnect()?.enqueue()
        manager = null
        logger = null
    }
}
