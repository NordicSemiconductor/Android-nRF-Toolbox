package no.nordicsemi.android.service

import android.bluetooth.BluetoothDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SelectedBluetoothDeviceHolder @Inject constructor() {

    var device: BluetoothDevice? = null
        private set

    fun isBondingRequired(): Boolean {
        return device?.bondState == BluetoothDevice.BOND_NONE
    }

    fun bondDevice() {
        device?.createBond()
    }

    fun attachDevice(newDevice: BluetoothDevice) {
        device = newDevice
    }

    fun forgetDevice() {
        device = null
    }
}
