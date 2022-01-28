package no.nordicsemi.android.prx.data

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.service.BleManagerStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PRXRepository @Inject constructor() {

    private val _data = MutableStateFlow(PRXData())
    val data: StateFlow<PRXData> = _data

    private val _command = MutableSharedFlow<PRXCommand>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val command = _command.asSharedFlow()

    private val _status = MutableStateFlow(BleManagerStatus.CONNECTING)
    val status = _status.asStateFlow()

    fun setBatteryLevel(batteryLevel: Int) {
        _data.tryEmit(_data.value.copy(batteryLevel = batteryLevel))
    }

    fun setLocalAlarmLevel(value: Int) {
        val alarmLevel = AlarmLevel.create(value)
        _data.tryEmit(_data.value.copy(localAlarmLevel = alarmLevel))
    }

    fun setRemoteAlarmLevel(isOn: Boolean) {
        _data.tryEmit(_data.value.copy(isRemoteAlarm = isOn))
    }

    fun invokeCommand(command: PRXCommand) {
        if (_command.subscriptionCount.value > 0) {
            _command.tryEmit(command)
        } else {
            _status.tryEmit(BleManagerStatus.DISCONNECTED)
        }
    }

    fun setNewStatus(status: BleManagerStatus) {
        _status.value = status
    }

    fun clear() {
        _status.value = BleManagerStatus.CONNECTING
        _data.tryEmit(PRXData())
    }
}
