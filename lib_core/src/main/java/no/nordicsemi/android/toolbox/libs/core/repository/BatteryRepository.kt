package no.nordicsemi.android.toolbox.libs.core.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.toolbox.libs.core.repository.data.BatteryServiceData

object BatteryRepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<BatteryServiceData>>()

    fun getData(deviceId: String): Flow<BatteryServiceData> {
        return _dataMap.getOrPut(deviceId) { MutableStateFlow(BatteryServiceData()) }
    }

    fun updateBatteryLevel(deviceId: String, data: Int) {
        _dataMap[deviceId]?.update { it.copy(batteryLevel = data) }
    }

    fun clear(deviceId: String) {
        _dataMap.remove(deviceId)
    }

}
