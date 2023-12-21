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

package no.nordicsemi.android.bps.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.os.ParcelUuid
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.analytics.AppAnalytics
import no.nordicsemi.android.analytics.Profile
import no.nordicsemi.android.analytics.ProfileConnectedEvent
import no.nordicsemi.android.bps.view.BPSViewEvent
import no.nordicsemi.android.bps.view.BPSViewState
import no.nordicsemi.android.bps.view.DisconnectEvent
import no.nordicsemi.android.bps.view.OpenLoggerEvent
import no.nordicsemi.android.common.logger.BleLoggerAndLauncher
import no.nordicsemi.android.common.logger.DefaultBleLogger
import no.nordicsemi.android.common.navigation.NavigationResult
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.kotlin.ble.profile.battery.BatteryLevelParser
import no.nordicsemi.android.kotlin.ble.profile.bps.BloodPressureMeasurementParser
import no.nordicsemi.android.kotlin.ble.profile.bps.IntermediateCuffPressureParser
import no.nordicsemi.android.kotlin.ble.profile.bps.data.BloodPressureMeasurementData
import no.nordicsemi.android.kotlin.ble.profile.bps.data.IntermediateCuffPressureData
import no.nordicsemi.android.toolbox.scanner.ScannerDestinationId
import no.nordicsemi.android.ui.view.StringConst
import java.util.UUID
import javax.inject.Inject

val BPS_SERVICE_UUID: UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb")
private val BPM_CHARACTERISTIC_UUID = UUID.fromString("00002A35-0000-1000-8000-00805f9b34fb")
private val ICP_CHARACTERISTIC_UUID = UUID.fromString("00002A36-0000-1000-8000-00805f9b34fb")

private val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
private val BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

@SuppressLint("MissingPermission", "StaticFieldLeak")
@HiltViewModel
internal class BPSViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val navigationManager: Navigator,
    private val analytics: AppAnalytics,
    private val stringConst: StringConst
) : ViewModel() {

    private val _state = MutableStateFlow(BPSViewState())
    val state = _state.asStateFlow()

    private var client: ClientBleGatt? = null
    private lateinit var logger: BleLoggerAndLauncher

    init {
        navigationManager.navigateTo(ScannerDestinationId, ParcelUuid(BPS_SERVICE_UUID))

        navigationManager.resultFrom(ScannerDestinationId)
            .onEach { handleArgs(it) }
            .launchIn(viewModelScope)
    }

    private fun handleArgs(result: NavigationResult<ServerDevice>) {
        when (result) {
            is NavigationResult.Cancelled -> navigationManager.navigateUp()
            is NavigationResult.Success -> startGattClient(result.value)
        }
    }

    fun onEvent(event: BPSViewEvent) {
        when (event) {
            DisconnectEvent -> onDisconnectEvent()
            OpenLoggerEvent -> logger.launch()
        }
    }

    private fun onDisconnectEvent() {
        client?.disconnect()
        navigationManager.navigateUp()
    }

    private fun startGattClient(device: ServerDevice) = viewModelScope.launch {
        _state.value = _state.value.copy(deviceName = device.name)

        logger = DefaultBleLogger.create(context, stringConst.APP_NAME, "BPS", device.address)

        val client = ClientBleGatt.connect(context, device, viewModelScope, logger = logger)
        this@BPSViewModel.client = client

        client.connectionStateWithStatus
            .filterNotNull()
            .onEach { onDataUpdate(it) }
            .onEach { logAnalytics(it.state) }
            .launchIn(viewModelScope)

        if (!client.isConnected) {
            return@launch
        }

        try {
            val services = client.discoverServices()
            configureGatt(services)
        } catch (e: Exception) {
            onMissingServices()
        }
    }

    private suspend fun configureGatt(services: ClientBleGattServices) {
        val bpsService = services.findService(BPS_SERVICE_UUID)!!
        val bpmCharacteristic = bpsService.findCharacteristic(BPM_CHARACTERISTIC_UUID)!!
        val icpCharacteristic = bpsService.findCharacteristic(ICP_CHARACTERISTIC_UUID)

        bpmCharacteristic.getNotifications()
            .mapNotNull { BloodPressureMeasurementParser.parse(it) }
            .onEach { onDataUpdate(it) }
            .catch { it.printStackTrace() }
            .launchIn(viewModelScope)

        icpCharacteristic?.getNotifications()
            ?.mapNotNull { IntermediateCuffPressureParser.parse(it) }
            ?.onEach { onDataUpdate(it) }
            ?.catch { it.printStackTrace() }
            ?.launchIn(viewModelScope)

        // Battery service is optional
        services.findService(BATTERY_SERVICE_UUID)
            ?.findCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)
            ?.getNotifications()
            ?.mapNotNull { BatteryLevelParser.parse(it) }
            ?.onEach { onDataUpdate(it) }
            ?.catch { it.printStackTrace() }
            ?.launchIn(viewModelScope)
    }

    private fun onMissingServices() {
        _state.value = _state.value.copy(missingServices = true)
        client?.disconnect()
    }

    private fun onDataUpdate(connectionState: GattConnectionStateWithStatus) {
        val newResult = _state.value.result.copy(connectionState = connectionState)
        _state.value = _state.value.copy(result = newResult)
    }

    private fun onDataUpdate(batteryLevel: Int) {
        val newResult = _state.value.result.copy(batteryLevel = batteryLevel)
        _state.value = _state.value.copy(result = newResult)
    }

    private fun onDataUpdate(data: BloodPressureMeasurementData) {
        val newResult = _state.value.result.copy(bloodPressureMeasurement = data)
        _state.value = _state.value.copy(result = newResult)
    }

    private fun onDataUpdate(data: IntermediateCuffPressureData) {
        val newResult = _state.value.result.copy(intermediateCuffPressure = data)
        _state.value = _state.value.copy(result = newResult)
    }

    private fun logAnalytics(connectionState: GattConnectionState) {
        if (connectionState == GattConnectionState.STATE_CONNECTED) {
            analytics.logEvent(ProfileConnectedEvent(Profile.BPS))
        }
    }
}
