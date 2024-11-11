package no.nordicsemi.android.service.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.service.handler.CGMHandler
import no.nordicsemi.android.toolbox.libs.core.data.CGMRecordWithSequenceNumber
import no.nordicsemi.android.toolbox.libs.core.data.CGMServiceData
import no.nordicsemi.android.toolbox.libs.core.data.gls.WorkingMode
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RequestStatus

object CGMRepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<CGMServiceData>>()

    fun getData(deviceId: String): StateFlow<CGMServiceData> =
        _dataMap.getOrPut(deviceId) { MutableStateFlow(CGMServiceData()) }

    fun clear(deviceId: String) {
        _dataMap.remove(deviceId)
    }

    fun onMeasurementDataReceived(deviceId: String, data: List<CGMRecordWithSequenceNumber>) {
        _dataMap[deviceId]?.update {
            it.copy(
                records = it.records + data

            )
        }
    }

    fun updateNewRequestStatus(deviceId: String, requestStatus: RequestStatus) {
        _dataMap[deviceId]?.update { it.copy(requestStatus = requestStatus) }
    }

    private fun clearState(deviceId: String) {
        _dataMap[deviceId]?.update {
            it.copy(
                records = emptyList(),
            )
        }
    }

    suspend fun requestRecord(deviceId: String, workingMode: WorkingMode) {
        clearState(deviceId)
        updateNewRequestStatus(deviceId, RequestStatus.PENDING)
        CGMHandler.requestRecord(deviceId, workingMode)
    }

}
