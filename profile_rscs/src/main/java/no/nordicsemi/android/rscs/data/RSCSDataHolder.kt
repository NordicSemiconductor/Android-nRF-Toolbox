package no.nordicsemi.android.rscs.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RSCSDataHolder @Inject constructor() {

    private val _data = MutableStateFlow(RSCSData())
    val data: StateFlow<RSCSData> = _data

    fun setNewData(
        running: Boolean,
        instantaneousSpeed: Float,
        instantaneousCadence: Int,
        strideLength: Int?,
        totalDistance: Long?
    ) {
        _data.tryEmit(_data.value.copy(
            running = running,
            instantaneousCadence = instantaneousCadence,
            instantaneousSpeed = instantaneousSpeed,
            strideLength = strideLength,
            totalDistance = totalDistance
        ))
    }

    fun setBatteryLevel(batteryLevel: Int) {
        _data.tryEmit(_data.value.copy(batteryLevel = batteryLevel))
    }

    fun clear() {
        _data.tryEmit(RSCSData())
    }
}
