package no.nordicsemi.android.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice

sealed class BleManagerResult <T> {

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

class IdleResult<T> : BleManagerResult<T>()
class ConnectingResult<T> : BleManagerResult<T>()
data class SuccessResult<T>(val device: BluetoothDevice, val data: T) : BleManagerResult<T>() {

    @SuppressLint("MissingPermission")
    fun deviceName(): String = device.name ?: device.address
}

class LinkLossResult<T>(val data: T) : BleManagerResult<T>()
class DisconnectedResult<T> : BleManagerResult<T>()
class UnknownErrorResult<T> : BleManagerResult<T>()
class MissingServiceResult<T> : BleManagerResult<T>()
