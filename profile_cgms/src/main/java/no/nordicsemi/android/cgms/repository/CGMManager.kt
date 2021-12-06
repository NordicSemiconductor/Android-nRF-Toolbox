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

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import android.util.SparseArray
import no.nordicsemi.android.ble.common.callback.RecordAccessControlPointDataCallback
import no.nordicsemi.android.ble.common.callback.cgm.CGMFeatureDataCallback
import no.nordicsemi.android.ble.common.callback.cgm.CGMSpecificOpsControlPointDataCallback
import no.nordicsemi.android.ble.common.callback.cgm.CGMStatusDataCallback
import no.nordicsemi.android.ble.common.callback.cgm.ContinuousGlucoseMeasurementDataCallback
import no.nordicsemi.android.ble.common.data.RecordAccessControlPointData
import no.nordicsemi.android.ble.common.data.cgm.CGMSpecificOpsControlPointData
import no.nordicsemi.android.ble.common.profile.RecordAccessControlPointCallback
import no.nordicsemi.android.ble.common.profile.cgm.CGMSpecificOpsControlPointCallback
import no.nordicsemi.android.ble.common.profile.cgm.CGMTypes
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.cgms.data.CGMDataHolder
import no.nordicsemi.android.cgms.data.CGMRecord
import no.nordicsemi.android.cgms.data.OnCGMValueReceived
import no.nordicsemi.android.cgms.data.OnDataSetCleared
import no.nordicsemi.android.cgms.data.OnNumberOfRecordsRequested
import no.nordicsemi.android.cgms.data.OnOperationAborted
import no.nordicsemi.android.cgms.data.OnOperationCompleted
import no.nordicsemi.android.cgms.data.OnOperationFailed
import no.nordicsemi.android.cgms.data.OnOperationNotSupported
import no.nordicsemi.android.cgms.data.OnOperationStarted
import no.nordicsemi.android.log.LogContract
import no.nordicsemi.android.service.BatteryManager
import java.util.*

/** Cycling Speed and Cadence service UUID.  */
val CGMS_SERVICE_UUID = UUID.fromString("0000181F-0000-1000-8000-00805f9b34fb")
private val CGM_STATUS_UUID = UUID.fromString("00002AA9-0000-1000-8000-00805f9b34fb")
private val CGM_FEATURE_UUID = UUID.fromString("00002AA8-0000-1000-8000-00805f9b34fb")
private val CGM_MEASUREMENT_UUID = UUID.fromString("00002AA7-0000-1000-8000-00805f9b34fb")
private val CGM_OPS_CONTROL_POINT_UUID =
    UUID.fromString("00002AAC-0000-1000-8000-00805f9b34fb")

/** Record Access Control Point characteristic UUID.  */
private val RACP_UUID = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb")

internal class CGMManager(
    context: Context,
    private val dataHolder: CGMDataHolder
) : BatteryManager(context) {

    private var cgmStatusCharacteristic: BluetoothGattCharacteristic? = null
    private var cgmFeatureCharacteristic: BluetoothGattCharacteristic? = null
    private var cgmMeasurementCharacteristic: BluetoothGattCharacteristic? = null
    private var cgmSpecificOpsControlPointCharacteristic: BluetoothGattCharacteristic? = null
    private var recordAccessControlPointCharacteristic: BluetoothGattCharacteristic? = null
    private val records: SparseArray<CGMRecord> = SparseArray<CGMRecord>()

    /** A flag set to true if the remote device supports E2E CRC.  */
    private var secured = false

    /**
     * A flag set when records has been requested using RACP. This is to distinguish CGM packets
     * received as continuous measurements or requested.
     */
    private var recordAccessRequestInProgress = false

    /**
     * The timestamp when the session has started. This is needed to display the user facing
     * times of samples.
     */
    private var sessionStartTime: Long = 0
    override fun onBatteryLevelChanged(batteryLevel: Int) {
        TODO("Not yet implemented")
    }

    override fun getGattCallback(): BatteryManagerGattCallback {
        return CGMManagerGattCallback()
    }

    /**
     * BluetoothGatt mCallbacks for connection/disconnection, service discovery,
     * receiving notification, etc.
     */
    private inner class CGMManagerGattCallback : BatteryManagerGattCallback() {
        override fun initialize() {
            // Enable Battery service
            super.initialize()

            // Read CGM Feature characteristic, mainly to see if the device supports E2E CRC.
            // This is not supported in the experimental CGMS from the SDK.
            readCharacteristic(cgmFeatureCharacteristic)
                .with(object : CGMFeatureDataCallback() {
                    override fun onContinuousGlucoseMonitorFeaturesReceived(
                        device: BluetoothDevice, features: CGMTypes.CGMFeatures,
                        type: Int, sampleLocation: Int, secured: Boolean
                    ) {
                        this@CGMManager.secured = features.e2eCrcSupported
                        log(
                            LogContract.Log.Level.APPLICATION,
                            "E2E CRC feature " + if (this@CGMManager.secured) "supported" else "not supported"
                        )
                    }
                })
                .fail { _: BluetoothDevice?, _: Int ->
                    log(
                        Log.WARN,
                        "Could not read CGM Feature characteristic"
                    )
                }
                .enqueue()

            // Check if the session is already started. This is not supported in the experimental CGMS from the SDK.
            readCharacteristic(cgmStatusCharacteristic)
                .with(object : CGMStatusDataCallback() {
                    override fun onContinuousGlucoseMonitorStatusChanged(
                        device: BluetoothDevice,
                        status: CGMTypes.CGMStatus,
                        timeOffset: Int,
                        secured: Boolean
                    ) {
                        if (!status.sessionStopped) {
                            sessionStartTime = System.currentTimeMillis() - timeOffset * 60000L
                            log(LogContract.Log.Level.APPLICATION, "Session already started")
                        }
                    }
                })
                .fail { _: BluetoothDevice?, _: Int ->
                    log(
                        Log.WARN,
                        "Could not read CGM Status characteristic"
                    )
                }
                .enqueue()

            // Set notification and indication mCallbacks
            setNotificationCallback(cgmMeasurementCharacteristic)
                .with(object : ContinuousGlucoseMeasurementDataCallback() {
                    override fun onDataReceived(device: BluetoothDevice, data: Data) {
                        log(
                            LogContract.Log.Level.APPLICATION,
                            "\"" + CGMMeasurementParser.parse(data).toString() + "\" received"
                        )
                        super.onDataReceived(device, data)
                    }

                    override fun onContinuousGlucoseMeasurementReceived(
                        device: BluetoothDevice,
                        glucoseConcentration: Float,
                        cgmTrend: Float?,
                        cgmQuality: Float?,
                        status: CGMTypes.CGMStatus?,
                        timeOffset: Int,
                        secured: Boolean
                    ) {
                        // If the CGM Status characteristic has not been read and the session was already started before,
                        // estimate the Session Start Time by subtracting timeOffset minutes from the current timestamp.
                        if (sessionStartTime == 0L && !recordAccessRequestInProgress) {
                            sessionStartTime = System.currentTimeMillis() - timeOffset * 60000L
                        }

                        // Calculate the sample timestamp based on the Session Start Time
                        val timestamp =
                            sessionStartTime + timeOffset * 60000L // Sequence number is in minutes since Start Session
                        val record = CGMRecord(timeOffset, glucoseConcentration, timestamp)
                        records.put(record.sequenceNumber, record)
                        dataHolder.emitNewEvent(OnCGMValueReceived(record))
                    }

                    override fun onContinuousGlucoseMeasurementReceivedWithCrcError(
                        device: BluetoothDevice,
                        data: Data
                    ) {
                        log(
                            Log.WARN,
                            "Continuous Glucose Measurement record received with CRC error"
                        )
                    }
                })
            setIndicationCallback(cgmSpecificOpsControlPointCharacteristic)
                .with(object : CGMSpecificOpsControlPointDataCallback() {
                    override fun onDataReceived(device: BluetoothDevice, data: Data) {
                        log(
                            LogContract.Log.Level.APPLICATION,
                            "\"" + CGMSpecificOpsControlPointParser.parse(data)
                                .toString() + "\" received"
                        )
                        super.onDataReceived(device, data)
                    }

                    @SuppressLint("SwitchIntDef")
                    override fun onCGMSpecificOpsOperationCompleted(
                        device: BluetoothDevice,
                        @CGMSpecificOpsControlPointCallback.CGMOpCode requestCode: Int,
                        secured: Boolean
                    ) {
                        when (requestCode) {
                            CGMSpecificOpsControlPointCallback.CGM_OP_CODE_START_SESSION -> sessionStartTime =
                                System.currentTimeMillis()
                            CGMSpecificOpsControlPointCallback.CGM_OP_CODE_STOP_SESSION -> sessionStartTime =
                                0
                        }
                    }

                    @SuppressLint("SwitchIntDef")
                    override fun onCGMSpecificOpsOperationError(
                        device: BluetoothDevice,
                        @CGMSpecificOpsControlPointCallback.CGMOpCode requestCode: Int,
                        @CGMSpecificOpsControlPointCallback.CGMErrorCode errorCode: Int,
                        secured: Boolean
                    ) {
                        when (requestCode) {
                            CGMSpecificOpsControlPointCallback.CGM_OP_CODE_START_SESSION -> {
                                if (errorCode == CGMSpecificOpsControlPointCallback.CGM_ERROR_PROCEDURE_NOT_COMPLETED) {
                                    // Session was already started before.
                                    // Looks like the CGM Status characteristic has not been read,
                                    // otherwise we would have got the Session Start Time before.
                                    // The Session Start Time will be calculated when a next CGM
                                    // packet is received based on it's Time Offset.
                                }
                                sessionStartTime = 0
                            }
                            CGMSpecificOpsControlPointCallback.CGM_OP_CODE_STOP_SESSION -> sessionStartTime =
                                0
                        }
                    }

                    override fun onCGMSpecificOpsResponseReceivedWithCrcError(
                        device: BluetoothDevice,
                        data: Data
                    ) {
                        log(Log.ERROR, "Request failed: CRC error")
                    }
                })
            setIndicationCallback(recordAccessControlPointCharacteristic)
                .with(object : RecordAccessControlPointDataCallback() {
                    override fun onDataReceived(device: BluetoothDevice, data: Data) {
                        log(
                            LogContract.Log.Level.APPLICATION,
                            "\"" + RecordAccessControlPointParser.parse(data)
                                .toString() + "\" received"
                        )
                        super.onDataReceived(device, data)
                    }

                    @SuppressLint("SwitchIntDef")
                    override fun onRecordAccessOperationCompleted(
                        device: BluetoothDevice,
                        @RecordAccessControlPointCallback.RACPOpCode requestCode: Int
                    ) {
                        when (requestCode) {
                            RecordAccessControlPointCallback.RACP_OP_CODE_ABORT_OPERATION -> dataHolder.emitNewEvent(
                                OnOperationAborted
                            )
                            else -> {
                                recordAccessRequestInProgress = false
                                dataHolder.emitNewEvent(OnOperationCompleted)
                            }
                        }
                    }

                    override fun onRecordAccessOperationCompletedWithNoRecordsFound(
                        device: BluetoothDevice,
                        @RecordAccessControlPointCallback.RACPOpCode requestCode: Int
                    ) {
                        recordAccessRequestInProgress = false
                        dataHolder.emitNewEvent(OnOperationCompleted)
                    }

                    override fun onNumberOfRecordsReceived(
                        device: BluetoothDevice,
                        numberOfRecords: Int
                    ) {
                        dataHolder.emitNewEvent(OnNumberOfRecordsRequested(numberOfRecords))
                        if (numberOfRecords > 0) {
                            if (records.size() > 0) {
                                val sequenceNumber = records.keyAt(records.size() - 1) + 1
                                writeCharacteristic(
                                    recordAccessControlPointCharacteristic,
                                    RecordAccessControlPointData.reportStoredRecordsGreaterThenOrEqualTo(
                                        sequenceNumber
                                    )
                                )
                                    .enqueue()
                            } else {
                                writeCharacteristic(
                                    recordAccessControlPointCharacteristic,
                                    RecordAccessControlPointData.reportAllStoredRecords()
                                )
                                    .enqueue()
                            }
                        } else {
                            recordAccessRequestInProgress = false
                            dataHolder.emitNewEvent(OnOperationCompleted)
                        }
                    }

                    override fun onRecordAccessOperationError(
                        device: BluetoothDevice,
                        @RecordAccessControlPointCallback.RACPOpCode requestCode: Int,
                        @RecordAccessControlPointCallback.RACPErrorCode errorCode: Int
                    ) {
                        log(Log.WARN, "Record Access operation failed (error $errorCode)")
                        if (errorCode == RecordAccessControlPointCallback.RACP_ERROR_OP_CODE_NOT_SUPPORTED) {
                            dataHolder.emitNewEvent(OnOperationNotSupported)
                        } else {
                            dataHolder.emitNewEvent(OnOperationFailed)
                        }
                    }
                })

            // Enable notifications and indications
            enableNotifications(cgmMeasurementCharacteristic)
                .fail { _: BluetoothDevice?, status: Int ->
                    log(
                        Log.WARN,
                        "Failed to enable Continuous Glucose Measurement notifications ($status)"
                    )
                }
                .enqueue()
            enableIndications(cgmSpecificOpsControlPointCharacteristic)
                .fail { _: BluetoothDevice?, status: Int ->
                    log(
                        Log.WARN,
                        "Failed to enable CGM Specific Ops Control Point indications notifications ($status)"
                    )
                }
                .enqueue()
            enableIndications(recordAccessControlPointCharacteristic)
                .fail { _: BluetoothDevice?, status: Int ->
                    log(
                        Log.WARN,
                        "Failed to enabled Record Access Control Point indications (error $status)"
                    )
                }
                .enqueue()

            // Start Continuous Glucose session if hasn't been started before
            if (sessionStartTime == 0L) {
                writeCharacteristic(
                    cgmSpecificOpsControlPointCharacteristic,
                    CGMSpecificOpsControlPointData.startSession(secured)
                )
                    .with { _: BluetoothDevice, data: Data ->
                        log(
                            LogContract.Log.Level.APPLICATION,
                            "\"" + CGMSpecificOpsControlPointParser.parse(data) + "\" sent"
                        )
                    }
                    .fail { _: BluetoothDevice?, status: Int ->
                        log(
                            LogContract.Log.Level.ERROR,
                            "Failed to start session (error $status)"
                        )
                    }
                    .enqueue()
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
            return cgmMeasurementCharacteristic != null && cgmSpecificOpsControlPointCharacteristic != null && recordAccessControlPointCharacteristic != null
        }

        override fun onServicesInvalidated() { }

        override fun onDeviceDisconnected() {
            super.onDeviceDisconnected()
            cgmStatusCharacteristic = null
            cgmFeatureCharacteristic = null
            cgmMeasurementCharacteristic = null
            cgmSpecificOpsControlPointCharacteristic = null
            recordAccessControlPointCharacteristic = null
        }
    }

    /**
     * Returns a list of CGM records obtained from this device. The key in the array is the
     */
    fun getRecords(): SparseArray<CGMRecord> {
        return records
    }

    /**
     * Clears the records list locally
     */
    fun clear() {
        records.clear()
        dataHolder.emitNewEvent(OnDataSetCleared)
    }

    /**
     * Sends the request to obtain the last (most recent) record from glucose device.
     * The data will be returned to Glucose Measurement characteristic as a notification followed by
     * Record Access Control Point indication with status code Success or other in case of error.
     */
    val lastRecord: Unit
        get() {
            if (recordAccessControlPointCharacteristic == null) return
            clear()
            dataHolder.emitNewEvent(OnOperationStarted)
            recordAccessRequestInProgress = true
            writeCharacteristic(
                recordAccessControlPointCharacteristic,
                RecordAccessControlPointData.reportLastStoredRecord()
            )
                .with { device: BluetoothDevice, data: Data ->
                    log(
                        LogContract.Log.Level.APPLICATION,
                        "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"
                    )
                }
                .enqueue()
        }

    /**
     * Sends the request to obtain the first (oldest) record from glucose device.
     * The data will be returned to Glucose Measurement characteristic as a notification followed by
     * Record Access Control Point indication with status code Success or other in case of error.
     */
    val firstRecord: Unit
        get() {
            if (recordAccessControlPointCharacteristic == null) return
            clear()
            dataHolder.emitNewEvent(OnOperationStarted)
            recordAccessRequestInProgress = true
            writeCharacteristic(
                recordAccessControlPointCharacteristic,
                RecordAccessControlPointData.reportFirstStoredRecord()
            )
                .with { _: BluetoothDevice, data: Data ->
                    log(
                        LogContract.Log.Level.APPLICATION,
                        "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"
                    )
                }
                .enqueue()
        }

    /**
     * Sends abort operation signal to the device.
     */
    fun abort() {
        if (recordAccessControlPointCharacteristic == null) return
        writeCharacteristic(
            recordAccessControlPointCharacteristic,
            RecordAccessControlPointData.abortOperation()
        )
            .with { _: BluetoothDevice, data: Data ->
                log(
                    LogContract.Log.Level.APPLICATION,
                    "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"
                )
            }
            .enqueue()
    }

    /**
     * Sends the request to obtain all records from glucose device. Initially we want to notify the
     * user about the number of the records so the Report Number of Stored Records request is send.
     * The data will be returned to Glucose Measurement characteristic as a notification followed by
     * Record Access Control Point indication with status code Success or other in case of error.
     */
    val allRecords: Unit
        get() {
            if (recordAccessControlPointCharacteristic == null) return
            clear()
            dataHolder.emitNewEvent(OnOperationStarted)
            recordAccessRequestInProgress = true
            writeCharacteristic(
                recordAccessControlPointCharacteristic,
                RecordAccessControlPointData.reportNumberOfAllStoredRecords()
            )
                .with { _: BluetoothDevice, data: Data ->
                    log(
                        LogContract.Log.Level.APPLICATION,
                        "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"
                    )
                }
                .enqueue()
        }

    /**
     * Sends the request to obtain all records from glucose device. Initially we want to notify the
     * user about the number of the records so the Report Number of Stored Records request is send.
     * The data will be returned to Glucose Measurement characteristic as a notification followed by
     * Record Access Control Point indication with status code Success or other in case of error.
     */
    fun refreshRecords() {
        if (recordAccessControlPointCharacteristic == null) return
        if (records.size() == 0) {
            allRecords
        } else {
            dataHolder.emitNewEvent(OnOperationStarted)

            // Obtain the last sequence number
            val sequenceNumber = records.keyAt(records.size() - 1) + 1
            recordAccessRequestInProgress = true
            writeCharacteristic(
                recordAccessControlPointCharacteristic,
                RecordAccessControlPointData.reportStoredRecordsGreaterThenOrEqualTo(sequenceNumber)
            )
                .with { _: BluetoothDevice, data: Data ->
                    log(
                        LogContract.Log.Level.APPLICATION,
                        "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"
                    )
                }
                .enqueue()
            // Info:
            // Operators OPERATOR_GREATER_THEN_OR_EQUAL, OPERATOR_LESS_THEN_OR_EQUAL and OPERATOR_RANGE are not supported by the CGMS sample from SDK
            // The "Operation not supported" response will be received
        }
    }

    /**
     * Sends the request to remove all stored records from the Continuous Glucose Monitor device.
     * This feature is not supported by the CGMS sample from the SDK, so monitor will answer with
     * the Op Code Not Supported error.
     */
    fun deleteAllRecords() {
        if (recordAccessControlPointCharacteristic == null) return
        clear()
        dataHolder.emitNewEvent(OnOperationStarted)
        writeCharacteristic(
            recordAccessControlPointCharacteristic,
            RecordAccessControlPointData.deleteAllStoredRecords()
        )
            .with { _: BluetoothDevice, data: Data ->
                log(
                    LogContract.Log.Level.APPLICATION,
                    "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"
                )
            }
            .enqueue()
    }
}
