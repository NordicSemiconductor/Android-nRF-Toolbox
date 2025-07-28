package no.nordicsemi.android.service.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.lib.profile.hts.HTSData
import no.nordicsemi.android.toolbox.libs.core.data.uiMapper.TemperatureUnit
import no.nordicsemi.android.toolbox.libs.core.data.HTSServiceData

object HTSRepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<HTSServiceData>>()

    fun getData(deviceId: String): Flow<HTSServiceData> {
        return _dataMap.getOrPut(deviceId) { MutableStateFlow(HTSServiceData()) }
    }

    fun updateHTSData(deviceId: String, data: HTSData) {
        _dataMap[deviceId]?.update { it.copy(data = data) }
    }

    fun clear(deviceId: String) {
        _dataMap.remove(deviceId)
    }

    fun onTemperatureUnitChange(deviceId: String, unit: TemperatureUnit) {
        _dataMap[deviceId]?.update { it.copy(temperatureUnit = unit) }
    }

}
