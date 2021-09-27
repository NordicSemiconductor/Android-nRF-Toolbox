package no.nordicsemi.android.csc.events

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

internal sealed class CSCServiceEvent : Parcelable

@Parcelize
internal data class OnDistanceChangedEvent(
    val bluetoothDevice: BluetoothDevice,
    val speed: Float,
    val distance: Float,
    val totalDistance: Float
) : CSCServiceEvent()

@Parcelize
internal data class CrankDataChanged(
    val bluetoothDevice: BluetoothDevice,
    val crankCadence: Int,
    val gearRatio: Float
) : CSCServiceEvent()

@Parcelize
internal data class OnBatteryLevelChanged(
    val device: BluetoothDevice,
    val batteryLevel: Int
) : CSCServiceEvent()
