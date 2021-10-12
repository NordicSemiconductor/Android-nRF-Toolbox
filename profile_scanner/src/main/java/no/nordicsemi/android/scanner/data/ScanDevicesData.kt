package no.nordicsemi.android.scanner.data

import android.bluetooth.BluetoothDevice

data class ScanDevicesData(
    val devices: List<BluetoothDevice> = emptyList()
) {

    fun copyWithNewDevice(device: BluetoothDevice): ScanDevicesData {
        if (devices.contains(device)) {
            return this
        }
        val newDevices = devices + device
        return copy(devices = newDevices)
    }

    fun copyWithNewDevices(bleDevices: List<BluetoothDevice>): ScanDevicesData {
        val filteredDevice = bleDevices.filter { !devices.contains(it) }
        val newDevices = devices + filteredDevice
        return copy(devices = newDevices)
    }
}
