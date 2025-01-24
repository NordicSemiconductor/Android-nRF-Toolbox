package no.nordicsemi.android.service.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import no.nordicsemi.android.service.services.ThroughputServiceManager
import no.nordicsemi.android.toolbox.libs.core.data.ThroughputServiceData

object ThroughputRepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<ThroughputServiceData>>()

    fun getData(deviceId: String): StateFlow<ThroughputServiceData> =
        _dataMap.getOrPut(deviceId) { MutableStateFlow(ThroughputServiceData()) }


    fun sendDataToDK(deviceId: String, scope: CoroutineScope) {
        ThroughputServiceManager.writeRequest(scope)
    }

    fun resetData(deviceId: String, scope: CoroutineScope) {
        ThroughputServiceManager.resetData(scope)
    }

}