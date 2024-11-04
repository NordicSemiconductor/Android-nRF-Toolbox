package no.nordicsemi.android.service.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.toolbox.libs.core.data.GLSServiceData
import no.nordicsemi.android.toolbox.libs.core.data.gls.RecordAccessControlPointInputParser
import no.nordicsemi.android.toolbox.libs.core.data.gls.WorkingMode
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSMeasurementContext
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSRecord
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RequestStatus
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.core.WriteType

object GLSRepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<GLSServiceData>>()

    private lateinit var recordAccessControlPointCharacteristic: RemoteCharacteristic

    fun getData(deviceId: String): StateFlow<GLSServiceData> = _dataMap.getOrPut(deviceId) {
        MutableStateFlow(GLSServiceData())
    }

    fun updateNewRecord(deviceId: String, record: GLSRecord) {
        val records = _dataMap[deviceId]?.value?.records?.toMutableMap()
        records?.set(record, null)
        if (records != null) {
            _dataMap[deviceId]?.update {
                it.copy(
                    records = records.toMap()
                )
            }
        }
    }

    fun updateRACPCharacteristics(remoteCharacteristic: RemoteCharacteristic) {
        recordAccessControlPointCharacteristic = remoteCharacteristic
    }

    fun updateWithNewContext(deviceId: String, context: GLSMeasurementContext) {
        val records = _dataMap[deviceId]?.value?.records?.toMutableMap()
        records?.keys?.firstOrNull { it.sequenceNumber == context.sequenceNumber }?.let {
            records[it] = context
        }
        if (records != null) {
            _dataMap[deviceId]?.update {
                it.copy(
                    records = records.toMap()
                )
            }
        }

    }

    suspend fun writeRecord(deviceId: String, workingMode: WorkingMode) {
        when (workingMode) {
            WorkingMode.ALL -> requestAllRecords(deviceId)
            WorkingMode.LAST -> requestLastRecord(deviceId)
            WorkingMode.FIRST -> requestFirstRecord(deviceId)
        }
    }

    private suspend fun requestLastRecord(deviceId: String) {
        clearState(deviceId)
        updateNewRequestStatus(deviceId, RequestStatus.PENDING)
        try {
            // Write to the characteristics.
            recordAccessControlPointCharacteristic.write(
                RecordAccessControlPointInputParser.reportLastStoredRecord(),
                WriteType.WITHOUT_RESPONSE
            )
        } catch (e: Exception) {
            e.printStackTrace()
            updateNewRequestStatus(deviceId, RequestStatus.FAILED)
        }
    }

    private suspend fun requestFirstRecord(deviceId: String) {
        clearState(deviceId)
        updateNewRequestStatus(deviceId, RequestStatus.PENDING)
        try {
            recordAccessControlPointCharacteristic.write(
                RecordAccessControlPointInputParser.reportFirstStoredRecord(),
                WriteType.WITHOUT_RESPONSE
            )
        } catch (e: Exception) {
            e.printStackTrace()
            clearState(deviceId)
            updateNewRequestStatus(deviceId, RequestStatus.FAILED)
        }
    }

    private suspend fun requestAllRecords(deviceId: String) {
        clearState(deviceId)
        clearState(deviceId)
        updateNewRequestStatus(deviceId, RequestStatus.PENDING)
        try {
            recordAccessControlPointCharacteristic.write(
                RecordAccessControlPointInputParser.reportNumberOfAllStoredRecords(),
                WriteType.WITHOUT_RESPONSE
            )
        } catch (e: Exception) {
            e.printStackTrace()
            clearState(deviceId)
            updateNewRequestStatus(deviceId, RequestStatus.FAILED)
        }
    }

    fun updateNewRequestStatus(deviceId: String, requestStatus: RequestStatus) {
        _dataMap[deviceId]?.update { it.copy(requestStatus = requestStatus) }
    }

    private fun clearState(deviceId: String) {
        _dataMap[deviceId]?.update {
            it.copy(
                records = mapOf(),
                requestStatus = RequestStatus.IDLE
            )
        }
    }

    fun clear(deviceId: String) {
        _dataMap.remove(deviceId)
    }

}

