package no.nordicsemi.android.service.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.toolbox.profile.data.CSCServiceData
import no.nordicsemi.android.lib.profile.csc.CSCData
import no.nordicsemi.android.lib.profile.csc.SpeedUnit
import no.nordicsemi.android.lib.profile.csc.WheelSize

object CSCRepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<CSCServiceData>>()

    fun getData(deviceId: String): StateFlow<CSCServiceData> = _dataMap.getOrPut(deviceId) {
        MutableStateFlow(CSCServiceData())
    }

    fun onCSCDataChanged(deviceId: String, cscData: CSCData) {
        _dataMap[deviceId]?.update { it.copy(data = cscData) }
    }

    fun setWheelSize(deviceId: String, wheelSize: WheelSize) {
        _dataMap[deviceId]?.update { currentValue ->
            currentValue.copy(
                data = CSCData(
                    wheelSize = wheelSize
                )
            )
        }
    }

    fun setSpeedUnit(deviceId: String, speedUnit: SpeedUnit) {
        _dataMap[deviceId]?.update { it.copy(speedUnit = speedUnit) }
    }

    fun clear(deviceId: String) {
        _dataMap.remove(deviceId)
    }

}
