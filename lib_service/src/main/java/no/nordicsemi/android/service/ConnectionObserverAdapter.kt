package no.nordicsemi.android.service

import android.bluetooth.BluetoothDevice
import android.util.Log
import no.nordicsemi.android.ble.observer.ConnectionObserver

abstract class ConnectionObserverAdapter : ConnectionObserver {

    private val TAG = "BLE-CONNECTION"

    override fun onDeviceConnecting(device: BluetoothDevice) {
        Log.d(TAG, "onDeviceConnecting()")
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        Log.d(TAG, "onDeviceConnected()")
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        Log.d(TAG, "onDeviceFailedToConnect()")
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        Log.d(TAG, "onDeviceReady()")
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        Log.d(TAG, "onDeviceDisconnecting()")
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        Log.d(TAG, "onDeviceDisconnected()")
    }
}
