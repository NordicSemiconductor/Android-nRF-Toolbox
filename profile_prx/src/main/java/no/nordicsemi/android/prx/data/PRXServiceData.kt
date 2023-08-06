package no.nordicsemi.android.prx.data

import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.kotlin.ble.profile.prx.AlarmLevel

data class PRXServiceData(
    val localAlarmLevel: AlarmLevel = AlarmLevel.NONE,
    val linkLossAlarmLevel: AlarmLevel = AlarmLevel.HIGH,
    val batteryLevel: Int? = null,
    val connectionState: GattConnectionStateWithStatus? = null,
    val connectionStatus: BleGattConnectionStatus? = null,
    val isRemoteAlarm: Boolean = false,
    val deviceName: String? = null,
    val missingServices: Boolean = false
) {

    val disconnectStatus = if (missingServices) {
        BleGattConnectionStatus.NOT_SUPPORTED
    } else {
        connectionState?.status ?: BleGattConnectionStatus.UNKNOWN
    }

    val isLinkLossDisconnected = connectionStatus?.isLinkLoss ?: false
}
