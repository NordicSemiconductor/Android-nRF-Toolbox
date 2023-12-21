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
import android.app.Application
import android.content.Context
import android.os.ParcelUuid
import androidx.lifecycle.AndroidViewModel
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
import no.nordicsemi.android.common.logger.BleLoggerAndLauncher
import no.nordicsemi.android.common.navigation.NavigationResult
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.gls.GlsDetailsDestinationId
import no.nordicsemi.android.gls.data.WorkingMode
import no.nordicsemi.android.gls.main.view.DisconnectEvent
import no.nordicsemi.android.gls.main.view.GLSScreenViewEvent
import no.nordicsemi.android.gls.main.view.GLSViewState
import no.nordicsemi.android.gls.main.view.OnGLSRecordClick
import no.nordicsemi.android.gls.main.view.OnWorkingModeSelected
import no.nordicsemi.android.gls.main.view.OpenLoggerEvent
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.kotlin.ble.core.errors.GattOperationException
import no.nordicsemi.android.kotlin.ble.profile.battery.BatteryLevelParser
import no.nordicsemi.android.kotlin.ble.profile.gls.GlucoseMeasurementContextParser
import no.nordicsemi.android.kotlin.ble.profile.gls.GlucoseMeasurementParser
import no.nordicsemi.android.kotlin.ble.profile.gls.RecordAccessControlPointInputParser
import no.nordicsemi.android.kotlin.ble.profile.gls.RecordAccessControlPointParser
import no.nordicsemi.android.kotlin.ble.profile.gls.data.GLSRecord
import no.nordicsemi.android.kotlin.ble.profile.gls.data.NumberOfRecordsData
import no.nordicsemi.android.kotlin.ble.profile.gls.data.RecordAccessControlPointData
import no.nordicsemi.android.kotlin.ble.profile.gls.data.RequestStatus
import no.nordicsemi.android.kotlin.ble.profile.gls.data.ResponseData
import no.nordicsemi.android.kotlin.ble.profile.racp.RACPOpCode
import no.nordicsemi.android.kotlin.ble.profile.racp.RACPResponseCode
import no.nordicsemi.android.toolbox.scanner.ScannerDestinationId
import no.nordicsemi.android.ui.view.NordicLoggerFactory
import no.nordicsemi.android.ui.view.StringConst
import no.nordicsemi.android.utils.tryOrLog
import java.util.*
import javax.inject.Inject

val GLS_SERVICE_UUID: UUID = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb")

val GLUCOSE_MEASUREMENT_CHARACTERISTIC = UUID.fromString("00002A18-0000-1000-8000-00805f9b34fb")
val GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC = UUID.fromString("00002A34-0000-1000-8000-00805f9b34fb")
val GLUCOSE_FEATURE_CHARACTERISTIC = UUID.fromString("00002A51-0000-1000-8000-00805f9b34fb")
val RACP_CHARACTERISTIC = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb")

val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
val BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

@SuppressLint("MissingPermission")
@HiltViewModel
internal class GLSViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val navigationManager: Navigator,
    private val analytics: AppAnalytics,
    private val stringConst: StringConst,
    private val loggerFactory: NordicLoggerFactory
) : AndroidViewModel(context as Application) {

    private var client: ClientBleGatt? = null
    private lateinit var logger: BleLoggerAndLauncher

    private lateinit var glucoseMeasurementCharacteristic: ClientBleGattCharacteristic
    private lateinit var recordAccessControlPointCharacteristic: ClientBleGattCharacteristic

    private val _state = MutableStateFlow(GLSViewState())
    val state = _state.asStateFlow()

    private val highestSequenceNumber
        get() = state.value.glsServiceData.records.keys.maxByOrNull { it.sequenceNumber }?.sequenceNumber ?: -1

    init {
        navigationManager.navigateTo(ScannerDestinationId, ParcelUuid(GLS_SERVICE_UUID))

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

    fun onEvent(event: GLSScreenViewEvent) {
        when (event) {
            OpenLoggerEvent -> logger.launch()
            is OnWorkingModeSelected -> onEvent(event)
            is OnGLSRecordClick -> navigateToDetails(event.record)
            DisconnectEvent -> onDisconnectEvent()
        }
    }

    private fun onDisconnectEvent() {
        client?.disconnect()
        navigationManager.navigateUp()
    }

    private fun navigateToDetails(record: GLSRecord) {
        val context = state.value.glsServiceData.records[record]
        navigationManager.navigateTo(GlsDetailsDestinationId, record to context)
    }

    private fun onDeviceSelected(device: ServerDevice) {
        startGattClient(device)
    }

    private fun onEvent(event: OnWorkingModeSelected) = viewModelScope.launch {
        when (event.workingMode) {
            WorkingMode.ALL -> requestAllRecords()
            WorkingMode.LAST -> requestLastRecord()
            WorkingMode.FIRST -> requestFirstRecord()
        }
    }

    private fun startGattClient(device: ServerDevice) = viewModelScope.launch {
        _state.value = _state.value.copy(deviceName = device.name)

        logger = loggerFactory.createNordicLogger(getApplication(), stringConst.APP_NAME, "GLS", device.address)

        val client = ClientBleGatt.connect(getApplication(), device, viewModelScope, logger = logger)
        this@GLSViewModel.client = client

        client.waitForBonding()

        client.connectionStateWithStatus
            .filterNotNull()
            .onEach { _state.value = _state.value.copyWithNewConnectionState(it) }
            .onEach { logAnalytics(it) }
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

    private fun onMissingServices() {
        _state.value = state.value.copy(missingServices = true)
        client?.disconnect()
    }

    internal fun logAnalytics(connectionState: GattConnectionStateWithStatus) {
        if (connectionState.state == GattConnectionState.STATE_CONNECTED) {
            analytics.logEvent(ProfileConnectedEvent(Profile.GLS))
        }
    }

    private suspend fun configureGatt(services: ClientBleGattServices) {
        val glsService = services.findService(GLS_SERVICE_UUID)!!
        glucoseMeasurementCharacteristic = glsService.findCharacteristic(GLUCOSE_MEASUREMENT_CHARACTERISTIC)!!
        recordAccessControlPointCharacteristic = glsService.findCharacteristic(RACP_CHARACTERISTIC)!!

        glucoseMeasurementCharacteristic.getNotifications()
            .mapNotNull { GlucoseMeasurementParser.parse(it) }
            .onEach { _state.value = _state.value.copyWithNewRecord(it) }
            .catch { it.printStackTrace() }
            .launchIn(viewModelScope)

        glsService.findCharacteristic(GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC)?.getNotifications()
            ?.mapNotNull { GlucoseMeasurementContextParser.parse(it) }
            ?.onEach { _state.value = _state.value.copyWithNewContext(it) }
            ?.catch { it.printStackTrace() }
            ?.launchIn(viewModelScope)

        recordAccessControlPointCharacteristic.getNotifications()
            .mapNotNull { RecordAccessControlPointParser.parse(it) }
            .onEach { onAccessControlPointDataReceived(it) }
            .catch { it.printStackTrace() }
            .launchIn(viewModelScope)

        // Battery service is optional
        services.findService(BATTERY_SERVICE_UUID)
            ?.findCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)
            ?.getNotifications()
            ?.mapNotNull { BatteryLevelParser.parse(it) }
            ?.onEach { _state.value = _state.value.copyWithNewBatteryLevel(it) }
            ?.catch { it.printStackTrace() }
            ?.launchIn(viewModelScope)
    }

    private fun onAccessControlPointDataReceived(data: RecordAccessControlPointData) = viewModelScope.launch {
        when (data) {
            is NumberOfRecordsData -> onNumberOfRecordsReceived(data.numberOfRecords)
            is ResponseData -> when (data.responseCode) {
                RACPResponseCode.RACP_RESPONSE_SUCCESS -> onRecordAccessOperationCompleted(data.requestCode)
                RACPResponseCode.RACP_ERROR_NO_RECORDS_FOUND -> onRecordAccessOperationCompletedWithNoRecordsFound()
                RACPResponseCode.RACP_ERROR_OP_CODE_NOT_SUPPORTED,
                RACPResponseCode.RACP_ERROR_INVALID_OPERATOR,
                RACPResponseCode.RACP_ERROR_OPERATOR_NOT_SUPPORTED,
                RACPResponseCode.RACP_ERROR_INVALID_OPERAND,
                RACPResponseCode.RACP_ERROR_ABORT_UNSUCCESSFUL,
                RACPResponseCode.RACP_ERROR_PROCEDURE_NOT_COMPLETED,
                RACPResponseCode.RACP_ERROR_OPERAND_NOT_SUPPORTED -> onRecordAccessOperationError(data.responseCode)
            }
        }
    }

    private fun onRecordAccessOperationCompleted(requestCode: RACPOpCode) {
        val status = when (requestCode) {
            RACPOpCode.RACP_OP_CODE_ABORT_OPERATION -> RequestStatus.ABORTED
            else -> RequestStatus.SUCCESS
        }
        _state.value = _state.value.copyWithNewRequestStatus(status)
    }

    private fun onRecordAccessOperationCompletedWithNoRecordsFound() {
        _state.value = _state.value.copyWithNewRequestStatus(RequestStatus.SUCCESS)
    }

    private suspend fun onNumberOfRecordsReceived(numberOfRecords: Int) {
        if (numberOfRecords > 0) {
            try {
                if (state.value.glsServiceData.records.isNotEmpty()) {
                    tryOrLog {
                        recordAccessControlPointCharacteristic.write(
                            RecordAccessControlPointInputParser.reportStoredRecordsGreaterThenOrEqualTo(highestSequenceNumber)
                        )
                    }
                } else {
                    tryOrLog {
                        recordAccessControlPointCharacteristic.write(
                            RecordAccessControlPointInputParser.reportAllStoredRecords()
                        )
                    }
                }
            } catch (e: GattOperationException) {
                e.printStackTrace()
            }
        }
        _state.value = _state.value.copyWithNewRequestStatus(RequestStatus.SUCCESS)
    }

    private fun onRecordAccessOperationError(response: RACPResponseCode) {
        if (response == RACPResponseCode.RACP_ERROR_OP_CODE_NOT_SUPPORTED) {
            _state.value = _state.value.copyWithNewRequestStatus(RequestStatus.NOT_SUPPORTED)
        } else {
            _state.value = _state.value.copyWithNewRequestStatus(RequestStatus.FAILED)
        }
    }

    private fun clear() {
        _state.value = _state.value.copyAndClear()
    }

    private suspend fun requestLastRecord() {
        clear()
        _state.value = _state.value.copyWithNewRequestStatus(RequestStatus.PENDING)
        try {
            recordAccessControlPointCharacteristic.write(RecordAccessControlPointInputParser.reportLastStoredRecord())
        } catch (e: Exception) {
            e.printStackTrace()
            _state.value = _state.value.copyWithNewRequestStatus(RequestStatus.FAILED)
        }
    }

    private suspend fun requestFirstRecord() {
        clear()
        _state.value = _state.value.copyWithNewRequestStatus(RequestStatus.PENDING)
        try {
            recordAccessControlPointCharacteristic.write(RecordAccessControlPointInputParser.reportFirstStoredRecord())
        } catch (e: Exception) {
            e.printStackTrace()
            _state.value = _state.value.copyWithNewRequestStatus(RequestStatus.FAILED)
        }
    }

    private suspend fun requestAllRecords() {
        clear()
        _state.value = _state.value.copyWithNewRequestStatus(RequestStatus.PENDING)
        try {
            recordAccessControlPointCharacteristic.write(RecordAccessControlPointInputParser.reportNumberOfAllStoredRecords())
        } catch (e: Exception) {
            e.printStackTrace()
            _state.value = _state.value.copyWithNewRequestStatus(RequestStatus.FAILED)
        }
    }
}
