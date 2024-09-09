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

package no.nordicsemi.android.cgms.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import no.nordicsemi.android.cgms.R
import no.nordicsemi.android.cgms.data.CGMRecordWithSequenceNumber
import no.nordicsemi.android.cgms.data.CGMServiceCommand
import no.nordicsemi.android.cgms.data.CGMServiceData
import no.nordicsemi.android.common.logger.LoggerLauncher
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.kotlin.ble.profile.gls.data.RequestStatus
import no.nordicsemi.android.log.LogSession
import no.nordicsemi.android.log.timber.nRFLoggerTree
import no.nordicsemi.android.service.DisconnectAndStopEvent
import no.nordicsemi.android.service.ServiceManager
import no.nordicsemi.android.toolbox.scanner.SelectedDevice
import no.nordicsemi.android.utils.simpleSharedFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CGMRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val serviceManager: ServiceManager,
) {
    private var tree: nRFLoggerTree? = null

    private val _data = MutableStateFlow(CGMServiceData())
    internal val data = _data.asStateFlow()

    private val _stopEvent = simpleSharedFlow<DisconnectAndStopEvent>()
    internal val stopEvent = _stopEvent.asSharedFlow()

    private val _command = simpleSharedFlow<CGMServiceCommand>()
    internal val command = _command.asSharedFlow()

    val isRunning = data.map { it.connectionState?.state == GattConnectionState.STATE_CONNECTED }
    val hasRecords = data.value.records.isNotEmpty()
    val highestSequenceNumber = data.value.records.maxOfOrNull { it.sequenceNumber } ?: -1

    private var isOnScreen = false
    private var isServiceRunning = false

    fun setOnScreen(isOnScreen: Boolean) {
        this.isOnScreen = isOnScreen

        if (shouldClean()) clean()
    }

    fun setServiceRunning(serviceRunning: Boolean) {
        this.isServiceRunning = serviceRunning

        if (shouldClean()) clean()
    }

    private fun shouldClean() = !isOnScreen && !isServiceRunning

    fun launch(device: SelectedDevice) {
        tree = nRFLoggerTree(context, "CGMS", device.device.address, device.name)
            .apply { setLoggingTagsEnabled(false) }
            .also { Timber.plant(it) }

        _data.value = _data.value.copy(deviceName = device.name)
        serviceManager.startService(CGMService::class.java, device.device, context.getString(R.string.cgms_title))
    }

    fun onDataReceived(data: List<CGMRecordWithSequenceNumber>) {
        _data.value = _data.value.copy(records = _data.value.records + data)
    }

    internal fun onCommand(command: CGMServiceCommand) {
        _command.tryEmit(command)
    }

    fun onConnectionStateChanged(connectionState: GattConnectionStateWithStatus?) {
        _data.value = _data.value.copy(connectionState = connectionState)
    }

    fun onBatteryLevelChanged(batteryLevel: Int) {
        _data.value = _data.value.copy(batteryLevel = batteryLevel)
    }

    fun onNewRequestStatus(requestStatus: RequestStatus) {
        _data.value = _data.value.copy(requestStatus = requestStatus)
    }

    fun onMissingServices() {
        _data.value = _data.value.copy(missingServices = true)
        _stopEvent.tryEmit(DisconnectAndStopEvent())
    }

    fun openLogger() {
        LoggerLauncher.launch(context, tree?.session as? LogSession)
    }

    fun clear() {
        _data.value = _data.value.copy(records = emptyList())
    }

    fun disconnect() {
        _stopEvent.tryEmit(DisconnectAndStopEvent())
    }

    private fun clean() {
        tree?.let { Timber.uproot(it) }
        tree = null
        _data.value = CGMServiceData()
    }
}
