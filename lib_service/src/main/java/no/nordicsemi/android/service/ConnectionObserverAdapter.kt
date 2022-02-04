package no.nordicsemi.android.service

import android.bluetooth.BluetoothDevice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.ble.observer.ConnectionObserver

class ConnectionObserverAdapter<T> : ConnectionObserver {

    private val TAG = "BLE-CONNECTION"

    private val _status = MutableStateFlow<BleManagerResult<T>>(ConnectingResult())
    val status = _status.asStateFlow()

    override fun onDeviceConnecting(device: BluetoothDevice) {
        Log.d(TAG, "onDeviceConnecting()")
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        Log.d(TAG, "onDeviceConnected()")
        _status.value = ReadyResult()
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        Log.d(TAG, "onDeviceFailedToConnect()")
        _status.value = DisconnectedResult()
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        Log.d(TAG, "onDeviceReady()")
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        Log.d(TAG, "onDeviceDisconnecting()")
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        Log.d(TAG, "onDeviceDisconnected()")
        _status.value = when (reason) {
            ConnectionObserver.REASON_NOT_SUPPORTED -> MissingServiceResult()
            ConnectionObserver.REASON_LINK_LOSS -> LinkLossResult()
            else -> DisconnectedResult()
        }
    }

    fun setValue(value: T) {
        _status.tryEmit(SuccessResult(value))
    }
}
