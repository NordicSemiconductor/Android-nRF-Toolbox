package no.nordicsemi.android.prx.data

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
        _command.tryEmit(command)
    }

    fun setNewStatus(status: BleManagerStatus) {
        _status.value = status
    }

    fun clear() {
        _status.value = BleManagerStatus.CONNECTING
        _data.tryEmit(PRXData())
    }
}
