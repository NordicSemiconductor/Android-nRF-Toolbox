package no.nordicsemi.android.hrs.data

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.service.BleManagerStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class HRSRepository @Inject constructor() {

    private val _data = MutableStateFlow(HRSData())
    val data: StateFlow<HRSData> = _data

    private val _command = MutableSharedFlow<DisconnectCommand>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)
    val command = _command.asSharedFlow()

    private val _status = MutableStateFlow(BleManagerStatus.CONNECTING)
    val status = _status.asStateFlow()

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

    fun sendDisconnectCommand() {
        if (_command.subscriptionCount.value > 0) {
            _command.tryEmit(DisconnectCommand)
        } else {
            _status.tryEmit(BleManagerStatus.DISCONNECTED)
        }
    }

    fun setNewStatus(status: BleManagerStatus) {
        _status.value = status
    }

    fun clear() {
        _status.value = BleManagerStatus.CONNECTING
        _data.tryEmit(HRSData())
    }
}
