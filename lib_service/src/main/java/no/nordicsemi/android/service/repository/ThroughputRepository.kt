package no.nordicsemi.android.service.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.lib.profile.throughput.ThroughputMetrics
import no.nordicsemi.android.service.services.ThroughputManager
import no.nordicsemi.android.toolbox.libs.core.data.ThroughputInputType
import no.nordicsemi.android.toolbox.libs.core.data.ThroughputServiceData
import no.nordicsemi.android.toolbox.libs.core.data.WritingStatus

object ThroughputRepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<ThroughputServiceData>>()

    fun getData(deviceId: String): StateFlow<ThroughputServiceData> =
        _dataMap.getOrPut(deviceId) { MutableStateFlow(ThroughputServiceData()) }

    fun updateThroughput(deviceId: String, throughputMetrics: ThroughputMetrics) {
        _dataMap[deviceId]?.update {
            it.copy(throughputData = throughputMetrics)
        }
    }

    suspend fun sendDataToDK(
        deviceId: String,
        writeDataType: ThroughputInputType,
    ) {
        val maxWriteValueLength = _dataMap[deviceId]?.value?.maxWriteValueLength ?: 20
        ThroughputManager.writeRequest(
            deviceId = deviceId,
            maxWriteValueLength = maxWriteValueLength,
            inputType = writeDataType,
        )
    }

    fun updateWriteStatus(deviceId: String, status: WritingStatus) {
        _dataMap[deviceId]?.update { it.copy(writingStatus = status) }
    }

    fun updateMaxWriteValueLength(deviceId: String, mtuSize: Int?) {
        _dataMap[deviceId]?.update { it.copy(maxWriteValueLength = mtuSize) }
    }

}