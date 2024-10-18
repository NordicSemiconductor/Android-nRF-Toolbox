package no.nordicsemi.android.toolbox.libs.profile.data.prx

data class PRXData(
    val localAlarmLevel: AlarmLevel = AlarmLevel.NONE,
    val isRemoteAlarm: Boolean = false,
    val linkLossAlarmLevel: AlarmLevel = AlarmLevel.HIGH
)