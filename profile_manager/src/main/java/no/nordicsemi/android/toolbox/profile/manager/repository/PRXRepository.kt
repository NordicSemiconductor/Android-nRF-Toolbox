package no.nordicsemi.android.toolbox.profile.manager.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.toolbox.profile.parser.prx.AlarmLevel
import no.nordicsemi.android.toolbox.profile.parser.prx.PRXData

object PRXRepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<PRXData>>()

    fun getData(deviceId: String): Flow<PRXData> {
        return _dataMap.getOrPut(deviceId) { MutableStateFlow(PRXData()) }
    }

    fun updatePRXData(deviceId: String, alarmLevel: AlarmLevel) {
        _dataMap[deviceId]?.update { it.copy(localAlarmLevel = alarmLevel) }
    }

    fun clear(deviceId: String) {
        _dataMap.remove(deviceId)
    }

    fun updateLinkLossAlarmLevelData(deviceId: String, linkLossAlarmLevel: AlarmLevel) {
        _dataMap[deviceId]?.update { it.copy(linkLossAlarmLevel = linkLossAlarmLevel) }
    }
}