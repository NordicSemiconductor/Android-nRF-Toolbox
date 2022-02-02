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
package no.nordicsemi.android.gls.repository

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.common.callback.RecordAccessControlPointDataCallback
import no.nordicsemi.android.ble.common.callback.RecordAccessControlPointResponse
import no.nordicsemi.android.ble.common.callback.glucose.GlucoseMeasurementContextResponse
import no.nordicsemi.android.ble.common.callback.glucose.GlucoseMeasurementResponse
import no.nordicsemi.android.ble.common.data.RecordAccessControlPointData
import no.nordicsemi.android.ble.ktx.asValidResponseFlow
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.gls.data.*
import no.nordicsemi.android.service.BatteryManager
import no.nordicsemi.android.service.CloseableCoroutineScope
import java.util.*
import javax.inject.Inject

val GLS_SERVICE_UUID: UUID = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb")

private val GM_CHARACTERISTIC = UUID.fromString("00002A18-0000-1000-8000-00805f9b34fb")
private val GM_CONTEXT_CHARACTERISTIC = UUID.fromString("00002A34-0000-1000-8000-00805f9b34fb")
private val GF_CHARACTERISTIC = UUID.fromString("00002A51-0000-1000-8000-00805f9b34fb")
private val RACP_CHARACTERISTIC = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb")

internal class GLSManager @Inject constructor(
    @ApplicationContext context: Context,
    private val repository: GLSRepository
) : BatteryManager(context) {

    private val scope = CloseableCoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var glucoseMeasurementCharacteristic: BluetoothGattCharacteristic? = null
    private var glucoseMeasurementContextCharacteristic: BluetoothGattCharacteristic? = null
    private var recordAccessControlPointCharacteristic: BluetoothGattCharacteristic? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, t->
        Log.e("COROUTINE-EXCEPTION", "Uncaught exception", t)
    }

    override fun onBatteryLevelChanged(batteryLevel: Int) {
        repository.setNewBatteryLevel(batteryLevel)
    }

    override fun getGattCallback(): BatteryManagerGattCallback {
        return GlucoseManagerGattCallback()
    }

    private inner class GlucoseManagerGattCallback : BatteryManagerGattCallback() {
        override fun initialize() {
            super.initialize()

            setNotificationCallback(glucoseMeasurementCharacteristic).asValidResponseFlow<GlucoseMeasurementResponse>()
                .onEach {
                    val record = GLSRecord(
                        sequenceNumber = it.sequenceNumber,
                        time = it.time,
                        glucoseConcentration = it.glucoseConcentration ?: 0f,
                        unit = it.unit?.let { ConcentrationUnit.create(it) }
                            ?: ConcentrationUnit.UNIT_KGPL,
                        type = RecordType.createOrNull(it.type),
                        sampleLocation = SampleLocation.createOrNull(it.sampleLocation),
                        status = it.status
                    )

                    repository.addNewRecord(record)
                }.launchIn(scope)

            setNotificationCallback(glucoseMeasurementContextCharacteristic).asValidResponseFlow<GlucoseMeasurementContextResponse>()
                .onEach {
                    val context = MeasurementContext(
                        sequenceNumber = it.sequenceNumber,
                        carbohydrate = it.carbohydrate,
                        carbohydrateAmount = it.carbohydrateAmount ?: 0f,
                        meal = it.meal,
                        tester = it.tester,
                        health = it.health,
                        exerciseDuration = it.exerciseDuration ?: 0,
                        exerciseIntensity = it.exerciseIntensity ?: 0,
                        medication = it.medication,
                        medicationQuantity = it.medicationAmount ?: 0f,
                        medicationUnit = it.medicationUnit?.let { MedicationUnit.create(it) }
                            ?: MedicationUnit.UNIT_KG,
                        HbA1c = it.hbA1c ?: 0f
                    )

                    repository.addNewContext(context)
                }.launchIn(scope)

            setIndicationCallback(recordAccessControlPointCharacteristic).asValidResponseFlow<RecordAccessControlPointResponse>()
                .onEach {
                    if (it.isOperationCompleted && it.wereRecordsFound() && it.numberOfRecords > 0) {
                        onNumberOfRecordsReceived(it)
                    } else if(it.isOperationCompleted && it.wereRecordsFound() && it.numberOfRecords == 0) {
                        onRecordAccessOperationCompletedWithNoRecordsFound(it)
                    } else if (it.isOperationCompleted && it.wereRecordsFound()) {
                        onRecordAccessOperationCompleted(it)
                    } else if (it.errorCode > 0) {
                        onRecordAccessOperationError(it)
                    }
                }.launchIn(scope)

            scope.launch(exceptionHandler) {
                enableNotifications(glucoseMeasurementCharacteristic).suspend()
            }
            scope.launch(exceptionHandler) {
                enableNotifications(glucoseMeasurementContextCharacteristic).suspend()
            }
            scope.launch(exceptionHandler) {
                enableIndications(recordAccessControlPointCharacteristic).suspend()
            }
        }

        private fun onRecordAccessOperationCompleted(response: RecordAccessControlPointResponse) {
            val status = when (response.requestCode) {
                RecordAccessControlPointDataCallback.RACP_OP_CODE_ABORT_OPERATION -> RequestStatus.ABORTED
                else -> RequestStatus.SUCCESS
            }
            repository.setRequestStatus(status)
        }

        private fun onRecordAccessOperationCompletedWithNoRecordsFound(response: RecordAccessControlPointResponse) {
            repository.setRequestStatus(RequestStatus.SUCCESS)
        }

        private suspend fun onNumberOfRecordsReceived(response: RecordAccessControlPointResponse) {
            if (response.numberOfRecords > 0) {
                if (repository.records().isNotEmpty()) {
                    val sequenceNumber = repository.records()
                        .last().sequenceNumber + 1 //TODO check if correct
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
            }
            repository.setRequestStatus(RequestStatus.SUCCESS)
        }

        private fun onRecordAccessOperationError(response: RecordAccessControlPointResponse) {
            log(Log.WARN, "Record Access operation failed (error ${response.errorCode})")
            if (response.errorCode == RecordAccessControlPointDataCallback.RACP_ERROR_OP_CODE_NOT_SUPPORTED) {
                repository.setRequestStatus(RequestStatus.NOT_SUPPORTED)
            } else {
                repository.setRequestStatus(RequestStatus.FAILED)
            }
        }

        public override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(GLS_SERVICE_UUID)
            if (service != null) {
                glucoseMeasurementCharacteristic = service.getCharacteristic(GM_CHARACTERISTIC)
                glucoseMeasurementContextCharacteristic = service.getCharacteristic(GM_CONTEXT_CHARACTERISTIC)
                recordAccessControlPointCharacteristic = service.getCharacteristic(RACP_CHARACTERISTIC)
            }
            return glucoseMeasurementCharacteristic != null && recordAccessControlPointCharacteristic != null
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
        repository.clearRecords()
        val target = bluetoothDevice
        if (target != null) {
            repository.setRequestStatus(RequestStatus.SUCCESS)
        }
    }

    fun requestLastRecord() {
        if (recordAccessControlPointCharacteristic == null) return
        val target = bluetoothDevice ?: return
        clear()
        repository.setRequestStatus(RequestStatus.PENDING)
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
        repository.setRequestStatus(RequestStatus.PENDING)
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
        repository.setRequestStatus(RequestStatus.PENDING)
        scope.launch(exceptionHandler) {
            writeCharacteristic(
                recordAccessControlPointCharacteristic,
                RecordAccessControlPointData.reportNumberOfAllStoredRecords(),
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            ).suspend()
        }
    }

    fun release() {
        scope.close()
    }
}
