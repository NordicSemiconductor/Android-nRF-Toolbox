package no.nordicsemi.android.hrs.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class HRSRepository @Inject constructor() {

    private val _data = MutableStateFlow(HRSData())
    val data: StateFlow<HRSData> = _data

    fun addNewHeartRate(heartRate: Int) {
        val result = _data.value.heartRates.toMutableList().apply {
            add(heartRate)
        }
        _data.tryEmit(_data.value.copy(heartRates = result))
    }

    fun setSensorLocation(sensorLocation: Int) {
        _data.tryEmit(_data.value.copy(sensorLocation = sensorLocation))
    }

    fun setBatteryLevel(batteryLevel: Int) {
        _data.tryEmit(_data.value.copy(batteryLevel = batteryLevel))
    }

    fun clear() {
        _data.tryEmit(HRSData())
    }
}
