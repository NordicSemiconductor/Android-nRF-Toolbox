package no.nordicsemi.android.hts.data

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.service.BleManagerStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class HTSRepository @Inject constructor() {

    private val _data = MutableStateFlow(HTSData())
    val data: StateFlow<HTSData> = _data

    private val _command = MutableSharedFlow<DisconnectCommand>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)
    val command = _command.asSharedFlow()

    private val _status = MutableStateFlow(BleManagerStatus.CONNECTING)
    val status = _status.asStateFlow()

    fun setNewTemperature(temperature: Float) {
        _data.tryEmit(_data.value.copy(temperatureValue = temperature))
    }

    fun setBatteryLevel(batteryLevel: Int) {
        _data.tryEmit(_data.value.copy(batteryLevel = batteryLevel))
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        _data.tryEmit(_data.value.copy(temperatureUnit = unit))
    }

    fun sendDisconnectCommand() {
        _command.tryEmit(DisconnectCommand)
    }

    fun setNewStatus(status: BleManagerStatus) {
        _status.value = status
    }

    fun clear() {
        _status.value = BleManagerStatus.CONNECTING
        _data.tryEmit(HTSData())
    }
}
