package no.nordicsemi.android.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice

sealed interface BleManagerResult<T> {

    fun isRunning(): Boolean {
        return this is SuccessResult
    }

    fun hasBeenDisconnected(): Boolean {
        return this is LinkLossResult || this is DisconnectedResult || this is MissingServiceResult
    }

    fun hasBeenDisconnectedWithoutLinkLoss(): Boolean {
        return this is DisconnectedResult || this is MissingServiceResult
    }
}

sealed class DeviceHolder(val device: BluetoothDevice) {

    @SuppressLint("MissingPermission")
    fun deviceName(): String = device.name ?: device.address

}

class IdleResult<T> : BleManagerResult<T>
class ConnectingResult<T>(device: BluetoothDevice) : DeviceHolder(device), BleManagerResult<T>
class ConnectedResult<T>(device: BluetoothDevice) : DeviceHolder(device), BleManagerResult<T>
class SuccessResult<T>(device: BluetoothDevice, val data: T) : DeviceHolder(device), BleManagerResult<T>

class LinkLossResult<T>(device: BluetoothDevice, val data: T?) : DeviceHolder(device), BleManagerResult<T>
class DisconnectedResult<T>(device: BluetoothDevice) : DeviceHolder(device), BleManagerResult<T>
class UnknownErrorResult<T>(device: BluetoothDevice) : DeviceHolder(device), BleManagerResult<T>
class MissingServiceResult<T>(device: BluetoothDevice) : DeviceHolder(device), BleManagerResult<T>
