package no.nordicsemi.android.prx.data

import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.profile.prx.AlarmLevel

data class PRXServiceData(
    val localAlarmLevel: AlarmLevel = AlarmLevel.NONE,
    val linkLossAlarmLevel: AlarmLevel = AlarmLevel.HIGH,
    val batteryLevel: Int? = null,
    val connectionState: GattConnectionState? = null,
    val isRemoteAlarm: Boolean = false,
    val deviceName: String? = null
)
