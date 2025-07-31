package no.nordicsemi.android.lib.profile.prx

data class PRXData(
    val localAlarmLevel: AlarmLevel = AlarmLevel.NONE,
    val isRemoteAlarm: Boolean = false,
    val linkLossAlarmLevel: AlarmLevel = AlarmLevel.HIGH
)