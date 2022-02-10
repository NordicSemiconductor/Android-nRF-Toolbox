/*
 * Copyright (c) 2016, Nordic Semiconductor
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
package no.nordicsemi.android.cgms.repository

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import android.util.SparseArray
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.common.callback.RecordAccessControlPointResponse
import no.nordicsemi.android.ble.common.callback.cgm.CGMFeatureResponse
import no.nordicsemi.android.ble.common.callback.cgm.CGMSpecificOpsControlPointResponse
import no.nordicsemi.android.ble.common.callback.cgm.CGMStatusResponse
import no.nordicsemi.android.ble.common.callback.cgm.ContinuousGlucoseMeasurementResponse
import no.nordicsemi.android.ble.common.data.RecordAccessControlPointData
import no.nordicsemi.android.ble.common.data.cgm.CGMSpecificOpsControlPointData
import no.nordicsemi.android.ble.common.profile.RecordAccessControlPointCallback
import no.nordicsemi.android.ble.common.profile.cgm.CGMSpecificOpsControlPointCallback
import no.nordicsemi.android.ble.ktx.asValidResponseFlow
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.ble.ktx.suspendForValidResponse
import no.nordicsemi.android.cgms.data.CGMData
import no.nordicsemi.android.cgms.data.CGMRecord
import no.nordicsemi.android.cgms.data.RequestStatus
import no.nordicsemi.android.service.ConnectionObserverAdapter
import java.util.*

val CGMS_SERVICE_UUID: UUID = UUID.fromString("0000181F-0000-1000-8000-00805f9b34fb")
private val CGM_STATUS_UUID = UUID.fromString("00002AA9-0000-1000-8000-00805f9b34fb")
private val CGM_FEATURE_UUID = UUID.fromString("00002AA8-0000-1000-8000-00805f9b34fb")
private val CGM_MEASUREMENT_UUID = UUID.fromString("00002AA7-0000-1000-8000-00805f9b34fb")
private val CGM_OPS_CONTROL_POINT_UUID = UUID.fromString("00002AAC-0000-1000-8000-00805f9b34fb")

private val RACP_UUID = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb")

private val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
private val BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

internal class CGMManager(
    context: Context,
    private val scope: CoroutineScope
) : BleManager(context) {

    private var cgmStatusCharacteristic: BluetoothGattCharacteristic? = null
    private var cgmFeatureCharacteristic: BluetoothGattCharacteristic? = null
    private var cgmMeasurementCharacteristic: BluetoothGattCharacteristic? = null
    private var cgmSpecificOpsControlPointCharacteristic: BluetoothGattCharacteristic? = null
    private var recordAccessControlPointCharacteristic: BluetoothGattCharacteristic? = null
    private val records: SparseArray<CGMRecord> = SparseArray<CGMRecord>()
    private var batteryLevelCharacteristic: BluetoothGattCharacteristic? = null

    private var secured = false

    private var recordAccessRequestInProgress = false

    private var sessionStartTime: Long = 0

    private val exceptionHandler = CoroutineExceptionHandler { _, t ->
        Log.e("COROUTINE-EXCEPTION", "Uncaught exception", t)
    }

    private val data = MutableStateFlow(CGMData())
    val dataHolder = ConnectionObserverAdapter<CGMData>()

    init {
        setConnectionObserver(dataHolder)

        data.onEach {
            dataHolder.setValue(it)
        }.launchIn(scope)
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return CGMManagerGattCallback()
    }

    private inner class CGMManagerGattCallback : BleManagerGattCallback() {
        override fun initialize() {
            super.initialize()

            scope.launch(exceptionHandler) {
                val response =
                    readCharacteristic(cgmFeatureCharacteristic).suspendForValidResponse<CGMFeatureResponse>()
                this@CGMManager.secured = response.features.e2eCrcSupported
            }

            scope.launch(exceptionHandler) {
                val response =
                    readCharacteristic(cgmFeatureCharacteristic).suspendForValidResponse<CGMFeatureResponse>()
                this@CGMManager.secured = response.features.e2eCrcSupported
            }

            scope.launch(exceptionHandler) {
                val response =
                    readCharacteristic(cgmStatusCharacteristic).suspendForValidResponse<CGMStatusResponse>()
                if (response.status?.sessionStopped == false) {
                    sessionStartTime = System.currentTimeMillis() - response.timeOffset * 60000L
                }
            }

            setNotificationCallback(cgmMeasurementCharacteristic).asValidResponseFlow<ContinuousGlucoseMeasurementResponse>()
                .onEach {
                    if (sessionStartTime == 0L && !recordAccessRequestInProgress) {
                        sessionStartTime = System.currentTimeMillis() - it.timeOffset * 60000L
                    }

                    val timestamp = sessionStartTime + it.timeOffset * 60000L
                    val record = CGMRecord(it.timeOffset, it.glucoseConcentration, timestamp)
                    records.put(record.sequenceNumber, record)

                    data.value = data.value.copy(records = records.toList())
                }.launchIn(scope)

            setIndicationCallback(cgmSpecificOpsControlPointCharacteristic).asValidResponseFlow<CGMSpecificOpsControlPointResponse>()
                .onEach {
                    if (it.isOperationCompleted) {
                        when (it.requestCode) {
                            CGMSpecificOpsControlPointCallback.CGM_OP_CODE_START_SESSION -> sessionStartTime =
                                System.currentTimeMillis()
                            CGMSpecificOpsControlPointCallback.CGM_OP_CODE_STOP_SESSION -> sessionStartTime =
                                0
                        }
                    } else {
                        when (it.requestCode) {
                            CGMSpecificOpsControlPointCallback.CGM_OP_CODE_START_SESSION ->
                                if (it.errorCode == CGMSpecificOpsControlPointCallback.CGM_ERROR_PROCEDURE_NOT_COMPLETED) {
                                    sessionStartTime = 0
                                }
                            CGMSpecificOpsControlPointCallback.CGM_OP_CODE_STOP_SESSION -> sessionStartTime =
                                0
                        }
                    }
                }.launchIn(scope)

            setIndicationCallback(recordAccessControlPointCharacteristic).asValidResponseFlow<RecordAccessControlPointResponse>()
                .onEach {
                    if (it.isOperationCompleted && !it.wereRecordsFound() && it.numberOfRecords > 0) {
                        onRecordsReceived(it)
                    } else if (it.isOperationCompleted && !it.wereRecordsFound()) {
                        onNoRecordsFound()
                    } else if (it.isOperationCompleted && it.wereRecordsFound()) {
                        onOperationCompleted(it)
                    } else if (it.errorCode > 0) {
                        onError(it)
                    }
                }.launchIn(scope)

            enableNotifications(cgmMeasurementCharacteristic).enqueue()
            enableIndications(cgmSpecificOpsControlPointCharacteristic).enqueue()
            enableIndications(recordAccessControlPointCharacteristic).enqueue()
            enableNotifications(batteryLevelCharacteristic).enqueue()

            if (sessionStartTime == 0L) {
                scope.launch(exceptionHandler) {
                    writeCharacteristic(
                        cgmSpecificOpsControlPointCharacteristic,
                        CGMSpecificOpsControlPointData.startSession(secured),
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    ).suspend()
                }
            }
        }

        private suspend fun onRecordsReceived(response: RecordAccessControlPointResponse) {
            if (response.numberOfRecords > 0) {
                if (records.size() > 0) {
                    val sequenceNumber = records.keyAt(records.size() - 1) + 1
                    writeCharacteristic(
                        recordAccessControlPointCharacteristic,
                        RecordAccessControlPointData.reportStoredRecordsGreaterThenOrEqualTo(sequenceNumber),
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    ).suspend()
                } else {
                    writeCharacteristic(
                        recordAccessControlPointCharacteristic,
                        RecordAccessControlPointData.reportAllStoredRecords(),
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    ).suspend()
                }
            } else {
                recordAccessRequestInProgress = false
                data.value = data.value.copy(requestStatus = RequestStatus.SUCCESS)
            }
        }

        private fun onNoRecordsFound() {
            recordAccessRequestInProgress = false
            data.value = data.value.copy(requestStatus = RequestStatus.SUCCESS)
        }

        private fun onOperationCompleted(response: RecordAccessControlPointResponse) {
            when (response.requestCode) {
                RecordAccessControlPointCallback.RACP_OP_CODE_ABORT_OPERATION ->
                    data.value = data.value.copy(requestStatus = RequestStatus.ABORTED)
                else -> {
                    recordAccessRequestInProgress = false
                    data.value = data.value.copy(requestStatus = RequestStatus.SUCCESS)
                }
            }
        }

        private fun onError(response: RecordAccessControlPointResponse) {
            if (response.errorCode == RecordAccessControlPointCallback.RACP_ERROR_OP_CODE_NOT_SUPPORTED) {
                data.value = data.value.copy(requestStatus = RequestStatus.NOT_SUPPORTED)
            } else {
                data.value = data.value.copy(requestStatus = RequestStatus.FAILED)
            }
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(CGMS_SERVICE_UUID)
            if (service != null) {
                cgmStatusCharacteristic = service.getCharacteristic(CGM_STATUS_UUID)
                cgmFeatureCharacteristic = service.getCharacteristic(CGM_FEATURE_UUID)
                cgmMeasurementCharacteristic = service.getCharacteristic(CGM_MEASUREMENT_UUID)
                cgmSpecificOpsControlPointCharacteristic = service.getCharacteristic(
                    CGM_OPS_CONTROL_POINT_UUID
                )
                recordAccessControlPointCharacteristic = service.getCharacteristic(RACP_UUID)
            }
            return cgmMeasurementCharacteristic != null && cgmSpecificOpsControlPointCharacteristic != null && recordAccessControlPointCharacteristic != null && cgmStatusCharacteristic != null && cgmFeatureCharacteristic != null
        }

        override fun onServicesInvalidated() {}

        override fun onDeviceDisconnected() {
            super.onDeviceDisconnected()
            cgmStatusCharacteristic = null
            cgmFeatureCharacteristic = null
            cgmMeasurementCharacteristic = null
            cgmSpecificOpsControlPointCharacteristic = null
            recordAccessControlPointCharacteristic = null
        }
    }

    private fun clear() {
        records.clear()
    }

    fun requestLastRecord() {
        if (recordAccessControlPointCharacteristic == null) return
        clear()
        data.value = data.value.copy(requestStatus = RequestStatus.PENDING)
        recordAccessRequestInProgress = true
        scope.launch(exceptionHandler) {
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
        data.value = data.value.copy(requestStatus = RequestStatus.PENDING)
        recordAccessRequestInProgress = true
        scope.launch(exceptionHandler) {
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
        data.value = data.value.copy(requestStatus = RequestStatus.PENDING)
        recordAccessRequestInProgress = true
        scope.launch(exceptionHandler) {
            writeCharacteristic(
                recordAccessControlPointCharacteristic,
                RecordAccessControlPointData.reportNumberOfAllStoredRecords(),
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            ).suspend()
        }
    }
}
