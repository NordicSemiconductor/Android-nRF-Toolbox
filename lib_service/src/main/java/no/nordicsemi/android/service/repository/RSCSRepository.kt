package no.nordicsemi.android.service.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.toolbox.profile.data.RSCSServiceData
import no.nordicsemi.android.lib.profile.rscs.RSCSData
import no.nordicsemi.android.lib.profile.rscs.RSCSSettingsUnit

object RSCSRepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<RSCSServiceData>>()

    fun getData(deviceId: String): Flow<RSCSServiceData> {
        return _dataMap.getOrPut(deviceId) { MutableStateFlow(RSCSServiceData()) }
    }

    fun clear(deviceId: String) {
        _dataMap.remove(deviceId)
    }

    fun onRSCSDataChanged(deviceId: String, data: RSCSData) {
        _dataMap[deviceId]?.update { it.copy(data = data) }
    }

    fun updateUnitSettings(deviceId: String, rscsUnitSettings: RSCSSettingsUnit) {
        _dataMap[deviceId]?.update { it.copy(unit = rscsUnitSettings) }
    }

}
