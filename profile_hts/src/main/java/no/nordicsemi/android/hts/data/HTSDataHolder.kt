package no.nordicsemi.android.hts.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class HTSDataHolder @Inject constructor() {

    private val _data = MutableStateFlow(HTSData())
    val data: StateFlow<HTSData> = _data

    fun setNewTemperature(temperature: Float) {
        _data.tryEmit(_data.value.copy(temperatureValue = temperature))
    }

    fun setBatteryLevel(batteryLevel: Int) {
        _data.tryEmit(_data.value.copy(batteryLevel = batteryLevel))
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        _data.tryEmit(_data.value.copy(temperatureUnit = unit))
    }

    fun clear() {
        _data.tryEmit(HTSData())
    }
}
