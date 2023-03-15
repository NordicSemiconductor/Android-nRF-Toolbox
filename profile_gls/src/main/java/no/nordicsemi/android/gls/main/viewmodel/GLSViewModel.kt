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

package no.nordicsemi.android.gls.main.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.ParcelUuid
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.analytics.AppAnalytics
import no.nordicsemi.android.analytics.Profile
import no.nordicsemi.android.analytics.ProfileConnectedEvent
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.common.navigation.NavigationResult
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.gls.GlsDetailsDestinationId
import no.nordicsemi.android.gls.data.GLS_SERVICE_UUID
import no.nordicsemi.android.gls.data.RequestStatus
import no.nordicsemi.android.gls.main.view.DisconnectEvent
import no.nordicsemi.android.gls.main.view.GLSScreenViewEvent
import no.nordicsemi.android.gls.main.view.GLSViewState
import no.nordicsemi.android.gls.main.view.OnGLSRecordClick
import no.nordicsemi.android.gls.main.view.OnWorkingModeSelected
import no.nordicsemi.android.gls.main.view.OpenLoggerEvent
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.client.callback.BleGattClient
import no.nordicsemi.android.kotlin.ble.core.client.service.BleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.client.service.BleGattServices
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.profile.battery.BatteryLevelParser
import no.nordicsemi.android.kotlin.ble.profile.gls.RecordAccessControlPointInputParser
import no.nordicsemi.android.kotlin.ble.profile.gls.data.RecordAccessControlPointData
import no.nordicsemi.android.kotlin.ble.profile.hrs.HRSDataParser
import no.nordicsemi.android.service.ConnectedResult
import no.nordicsemi.android.toolbox.scanner.ScannerDestinationId
import no.nordicsemi.android.utils.launchWithCatch
import java.util.*
import javax.inject.Inject

val GLS_SERVICE_UUID: UUID = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb")

private val GM_CHARACTERISTIC = UUID.fromString("00002A18-0000-1000-8000-00805f9b34fb")
private val GM_CONTEXT_CHARACTERISTIC = UUID.fromString("00002A34-0000-1000-8000-00805f9b34fb")
private val GF_CHARACTERISTIC = UUID.fromString("00002A51-0000-1000-8000-00805f9b34fb")
private val RACP_CHARACTERISTIC = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb")

private val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
private val BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

@SuppressLint("MissingPermission")
@HiltViewModel
internal class GLSViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val navigationManager: Navigator,
    private val analytics: AppAnalytics
) : ViewModel() {

    private lateinit var client: BleGattClient

    private lateinit var glucoseMeasurementCharacteristic: BleGattCharacteristic
    private lateinit var glucoseMeasurementContextCharacteristic: BleGattCharacteristic
    private lateinit var recordAccessControlPointCharacteristic: BleGattCharacteristic

    private val _state = MutableStateFlow(GLSViewState())
    val state = _state.asStateFlow()

    init {
        navigationManager.navigateTo(ScannerDestinationId, ParcelUuid(GLS_SERVICE_UUID))

        navigationManager.resultFrom(ScannerDestinationId)
            .onEach { handleResult(it) }
            .launchIn(viewModelScope)
    }

    private fun handleResult(result: NavigationResult<ServerDevice>) {
        when (result) {
            is NavigationResult.Cancelled -> navigationManager.navigateUp()
            is NavigationResult.Success -> onDeviceSelected(result.value)
        }
    }

    fun onEvent(event: GLSScreenViewEvent) {
        when (event) {
            OpenLoggerEvent -> repository.openLogger()
            DisconnectEvent -> navigationManager.navigateUp()
            is OnWorkingModeSelected -> repository.requestMode(event.workingMode)
            is OnGLSRecordClick -> navigationManager.navigateTo(GlsDetailsDestinationId, event.record)
            DisconnectEvent -> navigationManager.navigateUp()
        }
    }

    private fun onDeviceSelected(device: ServerDevice) {
        _state.value = _state.value.copy(deviceName = device.name)
        startGattClient(device)
    }

    private fun connectDevice(device: ServerDevice) {
        repository.downloadData(viewModelScope, device).onEach {
            _state.value = WorkingState(it)

            (it as? ConnectedResult)?.let {
                analytics.logEvent(ProfileConnectedEvent(Profile.GLS))
            }
        }.launchIn(viewModelScope)
    }

    private fun startGattClient(blinkyDevice: ServerDevice) = viewModelScope.launch {
        client = blinkyDevice.connect(context)

        client.connectionState
            .onEach { _state.value = _state.value.copy() }
            .filterNotNull()
            .onEach { stopIfDisconnected(it) }
            .launchIn(viewModelScope)

        client.services
            .filterNotNull()
            .onEach { configureGatt(it) }
            .launchIn(viewModelScope)
    }

    private suspend fun configureGatt(services: BleGattServices) {
        val glsService = services.findService(GLS_SERVICE_UUID)!!
        glucoseMeasurementCharacteristic = glsService.findCharacteristic(GM_CHARACTERISTIC)!!
        glucoseMeasurementContextCharacteristic = glsService.findCharacteristic(GM_CONTEXT_CHARACTERISTIC)!!
        recordAccessControlPointCharacteristic = glsService.findCharacteristic(RACP_CHARACTERISTIC)!!
        val batteryService = services.findService(BATTERY_SERVICE_UUID)!!
        val batteryLevelCharacteristic = batteryService.findCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)!!

        batteryLevelCharacteristic.getNotifications()
            .mapNotNull { BatteryLevelParser.parse(it) }
            .onEach { repository.onBatteryLevelChanged(it) }
            .launchIn(viewModelScope)

        htsMeasurementCharacteristic.getNotifications()
            .mapNotNull { HRSDataParser.parse(it) }
            .onEach { repository.onHRSDataChanged(it) }
            .launchIn(viewModelScope)
    }

    private fun stopIfDisconnected(connectionState: GattConnectionState) {
        if (connectionState == GattConnectionState.STATE_DISCONNECTED) {
            stopSelf()
        }
    }

    private fun clear() {
        _state.value = _state.value.copyAndClear()
    }

    suspend fun requestLastRecord() {
        recordAccessControlPointCharacteristic.write(RecordAccessControlPointInputParser.reportLastStoredRecord().value)
        clear()
        _state.value = _state.value.copyWithNewRequestStatus(RequestStatus.PENDING)
    }

    suspend fun requestFirstRecord() {
        recordAccessControlPointCharacteristic.write(RecordAccessControlPointInputParser.reportFirstStoredRecord().value)
        clear()
        _state.value = _state.value.copyWithNewRequestStatus(RequestStatus.PENDING)
    }

    suspend fun requestAllRecords() {
        recordAccessControlPointCharacteristic.write(RecordAccessControlPointInputParser.reportNumberOfAllStoredRecords().value)
        clear()
        _state.value = _state.value.copyWithNewRequestStatus(RequestStatus.PENDING)
    }
}
