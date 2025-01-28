package no.nordicsemi.android.service.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.lib.profile.throughput.ThroughputMetrics
import no.nordicsemi.android.service.services.ThroughputManager
import no.nordicsemi.android.toolbox.libs.core.data.ThroughputServiceData

object ThroughputRepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<ThroughputServiceData>>()

    fun getData(deviceId: String): StateFlow<ThroughputServiceData> =
        _dataMap.getOrPut(deviceId) { MutableStateFlow(ThroughputServiceData()) }


    fun sendDataToDK(deviceId: String, scope: CoroutineScope) {
        val isHighestMtuRequested = _dataMap[deviceId]?.value?.isHighestMtuRequested ?: false
        ThroughputManager.writeRequest(
            deviceId = deviceId,
            scope = scope,
            isHighestMtuRequested = isHighestMtuRequested
        )
    }

    fun resetData(deviceId: String, scope: CoroutineScope) {
        ThroughputManager.resetData(deviceId, scope)
    }

    fun updateThroughput(deviceId: String, throughputMetrics: ThroughputMetrics) {
        _dataMap[deviceId]?.update {
            it.copy(throughputData = throughputMetrics)
        }
    }

    fun mtuRequested(address: String) {
        _dataMap[address]?.update {
            it.copy(isHighestMtuRequested = true)
        }
    }

}