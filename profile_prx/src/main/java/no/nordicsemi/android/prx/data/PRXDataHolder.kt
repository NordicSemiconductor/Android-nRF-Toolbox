package no.nordicsemi.android.prx.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PRXDataHolder @Inject constructor() {

    private val _data = MutableStateFlow(PRXData())
    val data: StateFlow<PRXData> = _data

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

    fun clear(){
        _data.tryEmit(PRXData())
    }
}
