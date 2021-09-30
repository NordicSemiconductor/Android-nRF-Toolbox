package no.nordicsemi.android.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.companion.CompanionDeviceManager
import android.content.Context

class SelectedBluetoothDeviceHolder constructor(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?
) {

    val device: BluetoothDevice?
        get() {
            val deviceManager = context.getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
            return deviceManager.associations.firstOrNull()?.let { bluetoothAdapter?.getRemoteDevice(it) }
        }

    //TODO: Check if starts automatically
    fun bondDevice() {
        device?.let {
            if (it.bondState == BluetoothDevice.BOND_NONE) {
                it.createBond()
            }
        }
    }

    fun forgetDevice() {
        device?.let {
            val deviceManager = context.getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
            deviceManager.disassociate(it.address)
        }
    }
}
