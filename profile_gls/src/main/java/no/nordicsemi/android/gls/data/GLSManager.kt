/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.nordicsemi.android.gls.data

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.common.callback.RecordAccessControlPointDataCallback
import no.nordicsemi.android.ble.common.callback.RecordAccessControlPointResponse
import no.nordicsemi.android.ble.common.callback.battery.BatteryLevelResponse
import no.nordicsemi.android.ble.common.callback.glucose.GlucoseMeasurementContextResponse
import no.nordicsemi.android.ble.common.callback.glucose.GlucoseMeasurementResponse
import no.nordicsemi.android.ble.common.data.RecordAccessControlPointData
import no.nordicsemi.android.ble.ktx.asValidResponseFlow
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.service.ConnectionObserverAdapter
import no.nordicsemi.android.utils.launchWithCatch
import java.util.*
import javax.inject.Inject

val GLS_SERVICE_UUID: UUID = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb")

private val GM_CHARACTERISTIC = UUID.fromString("00002A18-0000-1000-8000-00805f9b34fb")
private val GM_CONTEXT_CHARACTERISTIC = UUID.fromString("00002A34-0000-1000-8000-00805f9b34fb")
private val GF_CHARACTERISTIC = UUID.fromString("00002A51-0000-1000-8000-00805f9b34fb")
private val RACP_CHARACTERISTIC = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb")

private val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
private val BATTERY_LEVEL_CHARACTERISTIC_UUID =
    UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

internal class GLSManager @Inject constructor(
    @ApplicationContext
    context: Context,
    private val scope: CoroutineScope
) : BleManager(context) {

    private var batteryLevelCharacteristic: BluetoothGattCharacteristic? = null
    private var glucoseMeasurementCharacteristic: BluetoothGattCharacteristic? = null
    private var glucoseMeasurementContextCharacteristic: BluetoothGattCharacteristic? = null
    private var recordAccessControlPointCharacteristic: BluetoothGattCharacteristic? = null

    private val data = MutableStateFlow(GLSData())
    val dataHolder = ConnectionObserverAdapter<GLSData>()

    init {
        setConnectionObserver(dataHolder)

        data.onEach {
            dataHolder.setValue(it)
        }.launchIn(scope)
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return GlucoseManagerGattCallback()
    }

    private inner class GlucoseManagerGattCallback : BleManagerGattCallback() {
        override fun initialize() {
            super.initialize()

            setNotificationCallback(glucoseMeasurementCharacteristic).asValidResponseFlow<GlucoseMeasurementResponse>()
                .onEach { data.tryEmit(data.value.copy(records = data.value.records + it.toRecord())) }
                .launchIn(scope)

            setNotificationCallback(glucoseMeasurementContextCharacteristic).asValidResponseFlow<GlucoseMeasurementContextResponse>()
                .onEach {
                    val context = it.toMeasurementContext()
                    data.value.records.find { context.sequenceNumber == it.sequenceNumber }?.let {
                        it.context = context
                    }
                    data.tryEmit(data.value)
                }.launchIn(scope)

            setIndicationCallback(recordAccessControlPointCharacteristic).asValidResponseFlow<RecordAccessControlPointResponse>()
                .onEach {
                    if (it.isOperationCompleted && it.wereRecordsFound() && it.numberOfRecords > 0) {
                        onNumberOfRecordsReceived(it)
                    } else if (it.isOperationCompleted && it.wereRecordsFound() && it.numberOfRecords == 0) {
                        onRecordAccessOperationCompletedWithNoRecordsFound(it)
                    } else if (it.isOperationCompleted && it.wereRecordsFound()) {
                        onRecordAccessOperationCompleted(it)
                    } else if (it.errorCode > 0) {
                        onRecordAccessOperationError(it)
                    }
                }.launchIn(scope)

            setNotificationCallback(batteryLevelCharacteristic).asValidResponseFlow<BatteryLevelResponse>()
                .onEach {
                    data.value = data.value.copy(batteryLevel = it.batteryLevel)
                }.launchIn(scope)

            enableNotifications(glucoseMeasurementCharacteristic).enqueue()
            enableNotifications(glucoseMeasurementContextCharacteristic).enqueue()
            enableIndications(recordAccessControlPointCharacteristic).enqueue()
            enableNotifications(batteryLevelCharacteristic).enqueue()
        }

        private fun onRecordAccessOperationCompleted(response: RecordAccessControlPointResponse) {
            val status = when (response.requestCode) {
                RecordAccessControlPointDataCallback.RACP_OP_CODE_ABORT_OPERATION -> RequestStatus.ABORTED
                else -> RequestStatus.SUCCESS
            }
            data.tryEmit(data.value.copy(requestStatus = status))
        }

        private fun onRecordAccessOperationCompletedWithNoRecordsFound(response: RecordAccessControlPointResponse) {
            data.tryEmit(data.value.copy(requestStatus = RequestStatus.SUCCESS))
        }

        private suspend fun onNumberOfRecordsReceived(response: RecordAccessControlPointResponse) {
            if (response.numberOfRecords > 0) {
                if (data.value.records.isNotEmpty()) {
                    val sequenceNumber = data.value.records
                        .last().sequenceNumber + 1
                    writeCharacteristic(
                        recordAccessControlPointCharacteristic,
                        RecordAccessControlPointData.reportStoredRecordsGreaterThenOrEqualTo(
                            sequenceNumber
                        ),
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    ).suspend()
                } else {
                    writeCharacteristic(
                        recordAccessControlPointCharacteristic,
                        RecordAccessControlPointData.reportAllStoredRecords(),
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    ).suspend()
                }
            }
            data.tryEmit(data.value.copy(requestStatus = RequestStatus.SUCCESS))
        }

        private fun onRecordAccessOperationError(response: RecordAccessControlPointResponse) {
            log(Log.WARN, "Record Access operation failed (error ${response.errorCode})")
            if (response.errorCode == RecordAccessControlPointDataCallback.RACP_ERROR_OP_CODE_NOT_SUPPORTED) {
                data.tryEmit(data.value.copy(requestStatus = RequestStatus.NOT_SUPPORTED))
            } else {
                data.tryEmit(data.value.copy(requestStatus = RequestStatus.FAILED))
            }
        }

        public override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            gatt.getService(GLS_SERVICE_UUID)?.run {
                glucoseMeasurementCharacteristic = getCharacteristic(GM_CHARACTERISTIC)
                glucoseMeasurementContextCharacteristic = getCharacteristic(GM_CONTEXT_CHARACTERISTIC)
                recordAccessControlPointCharacteristic = getCharacteristic(RACP_CHARACTERISTIC)
            }
            gatt.getService(BATTERY_SERVICE_UUID)?.run {
                batteryLevelCharacteristic = getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)
            }
            return glucoseMeasurementCharacteristic != null && recordAccessControlPointCharacteristic != null && glucoseMeasurementContextCharacteristic != null && batteryLevelCharacteristic != null
        }

        override fun onServicesInvalidated() {}

        override fun isOptionalServiceSupported(gatt: BluetoothGatt): Boolean {
            super.isOptionalServiceSupported(gatt)
            return glucoseMeasurementContextCharacteristic != null
        }

        override fun onDeviceDisconnected() {
            glucoseMeasurementCharacteristic = null
            glucoseMeasurementContextCharacteristic = null
            recordAccessControlPointCharacteristic = null
        }
    }

    private fun clear() {
        data.tryEmit(data.value.copy(records = emptyList()))
        val target = bluetoothDevice
        if (target != null) {
            data.tryEmit(data.value.copy(requestStatus = RequestStatus.SUCCESS))
        }
    }

    fun requestLastRecord() {
        if (recordAccessControlPointCharacteristic == null) return
        val target = bluetoothDevice ?: return
        clear()
        data.tryEmit(data.value.copy(requestStatus = RequestStatus.PENDING))
        scope.launchWithCatch {
            writeCharacteristic(
                recordAccessControlPointCharacteristic,
                RecordAccessControlPointData.reportLastStoredRecord(),
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            ).suspend()
        }
    }

    fun requestFirstRecord() {
        if (recordAccessControlPointCharacteristic == null) return
        clear()
        data.tryEmit(data.value.copy(requestStatus = RequestStatus.PENDING))
        scope.launchWithCatch {
            writeCharacteristic(
                recordAccessControlPointCharacteristic,
                RecordAccessControlPointData.reportFirstStoredRecord(),
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            ).suspend()
        }
    }

    fun requestAllRecords() {
        if (recordAccessControlPointCharacteristic == null) return
        clear()
        data.tryEmit(data.value.copy(requestStatus = RequestStatus.PENDING))
        scope.launchWithCatch {
            writeCharacteristic(
                recordAccessControlPointCharacteristic,
                RecordAccessControlPointData.reportNumberOfAllStoredRecords(),
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            ).suspend()
        }
    }
}
