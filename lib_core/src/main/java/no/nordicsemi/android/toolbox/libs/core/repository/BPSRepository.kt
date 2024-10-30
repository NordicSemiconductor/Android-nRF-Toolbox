package no.nordicsemi.android.toolbox.libs.core.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.toolbox.libs.core.data.bps.BloodPressureMeasurementData
import no.nordicsemi.android.toolbox.libs.core.data.bps.IntermediateCuffPressureData
import no.nordicsemi.android.toolbox.libs.core.repository.data.BPSServiceData

object BPSRepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<BPSServiceData>>()

    fun getData(deviceId: String): Flow<BPSServiceData> {
        return _dataMap.getOrPut(deviceId) { MutableStateFlow(BPSServiceData()) }
    }

    fun updateBPSData(deviceId: String, bpsData: BloodPressureMeasurementData) {
        _dataMap[deviceId]?.update { it.copy(bloodPressureMeasurement = bpsData) }
    }

    fun clear(deviceId: String) {
        _dataMap.remove(deviceId)
    }

    fun updateICPData(deviceId: String, icpData: IntermediateCuffPressureData) {
        _dataMap[deviceId]?.update { it.copy(intermediateCuffPressure = icpData) }
    }

}