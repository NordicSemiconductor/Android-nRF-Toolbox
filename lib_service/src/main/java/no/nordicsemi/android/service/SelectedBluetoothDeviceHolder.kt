package no.nordicsemi.android.service

import android.bluetooth.BluetoothDevice

class SelectedBluetoothDeviceHolder {

    var device: BluetoothDevice? = null
        private set

    fun isBondingRequired(): Boolean {
        return device?.bondState == BluetoothDevice.BOND_NONE
    }

    fun getBondingState(): BondingState {
        return when (device?.bondState) {
            BluetoothDevice.BOND_BONDED -> BondingState.BONDED
            BluetoothDevice.BOND_BONDING -> BondingState.BONDING
            else -> BondingState.NONE
        }
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

enum class BondingState {
    NONE, BONDING, BONDED;

    companion object {
        fun create(value: Int): BondingState {
            return when (value) {
                BluetoothDevice.BOND_BONDED -> BONDED
                BluetoothDevice.BOND_BONDING -> BONDING
                BluetoothDevice.BOND_NONE -> NONE
                else -> throw IllegalArgumentException("Cannot create BondingState for the value: $value")
            }
        }
    }
}
