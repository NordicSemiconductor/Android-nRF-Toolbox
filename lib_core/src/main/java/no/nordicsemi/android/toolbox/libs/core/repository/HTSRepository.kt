package no.nordicsemi.android.toolbox.libs.core.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.toolbox.libs.core.data.hts.HtsData
import no.nordicsemi.android.toolbox.libs.core.data.hts.TemperatureUnit
import no.nordicsemi.android.toolbox.libs.core.repository.data.HTSServiceData

object HTSRepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<HTSServiceData>>()

    fun getData(deviceId: String): Flow<HTSServiceData> {
        return _dataMap.getOrPut(deviceId) { MutableStateFlow(HTSServiceData()) }
    }

    fun updateHTSData(deviceId: String, data: HtsData) {
        _dataMap[deviceId]?.update { it.copy(data = data) }
    }

    fun clear(deviceId: String) {
        _dataMap.remove(deviceId)
    }

    fun onTemperatureUnitChange(deviceId: String, unit: TemperatureUnit) {
        _dataMap[deviceId]?.update { it.copy(temperatureUnit = unit) }
    }

}
