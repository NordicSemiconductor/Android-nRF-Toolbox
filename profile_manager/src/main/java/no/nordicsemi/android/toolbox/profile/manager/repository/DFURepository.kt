package no.nordicsemi.android.toolbox.profile.manager.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.toolbox.profile.data.DFUServiceData

object DFURepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<DFUServiceData>>()

    fun getData(deviceId: String): Flow<DFUServiceData> {
        return _dataMap.getOrPut(deviceId) { MutableStateFlow(DFUServiceData()) }
    }

    fun updateAppName(deviceId: String, appName: String) {
        _dataMap[deviceId]?.update { it.copy(dfuAppName = appName) }
    }

    fun clear(deviceId: String) {
        _dataMap.remove(deviceId)
    }

}