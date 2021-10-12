package no.nordicsemi.android.scanner.view

import android.bluetooth.BluetoothDevice

sealed class ScanDevicesViewEvent

data class OnDeviceSelected(val device: BluetoothDevice) : ScanDevicesViewEvent()

object OnCancelButtonClick : ScanDevicesViewEvent()

fun BluetoothDevice.displayName(): String {
    return name ?: address
}
