package no.nordicsemi.android.toolbox.libs.core.data.prx

data class PRXData(
    val localAlarmLevel: AlarmLevel = AlarmLevel.NONE,
    val isRemoteAlarm: Boolean = false,
    val linkLossAlarmLevel: AlarmLevel = AlarmLevel.HIGH
)