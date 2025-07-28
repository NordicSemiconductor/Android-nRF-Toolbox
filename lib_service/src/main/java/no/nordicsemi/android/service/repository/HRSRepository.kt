package no.nordicsemi.android.service.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.toolbox.libs.core.data.hrs.HRSData
import no.nordicsemi.android.toolbox.libs.core.data.HRSServiceData

object HRSRepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<HRSServiceData>>()

    fun getData(deviceId: String): Flow<HRSServiceData> {
        return _dataMap.getOrPut(deviceId) { MutableStateFlow(HRSServiceData()) }
    }

    fun updateHRSData(deviceId: String, data: HRSData) {
        _dataMap[deviceId]?.update { it.copy(data = it.data + data) }
    }

    fun clear(deviceId: String) {
        _dataMap.remove(deviceId)
    }

    fun updateBodySensorLocation(deviceId: String, location: Int) {
        _dataMap[deviceId]?.update { it.copy(bodySensorLocation = location) }
    }

    fun updateZoomIn(deviceId: String) {
        _dataMap[deviceId]?.update { it.copy(zoomIn = !it.zoomIn) }
    }
}
