package no.nordicsemi.android.service

import android.bluetooth.BluetoothDevice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.ble.observer.ConnectionObserver

class ConnectionObserverAdapter<T> : ConnectionObserver {

    private val TAG = "BLE-CONNECTION"

    private val _status = MutableStateFlow<BleManagerResult<T>>(IdleResult())
    val status = _status.asStateFlow()

    private var lastValue: T? = null

    private fun getData(): T? {
        return (_status.value as? SuccessResult)?.data
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        Log.d(TAG, "onDeviceConnecting()")
        _status.value = ConnectingResult(device)
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        Log.d(TAG, "onDeviceConnected()")
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        Log.d(TAG, "onDeviceFailedToConnect(), reason: $reason")
        _status.value = MissingServiceResult(device)
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        Log.d(TAG, "onDeviceReady()")
        _status.value = SuccessResult(device, lastValue!!)
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        Log.d(TAG, "onDeviceDisconnecting()")
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        Log.d(TAG, "onDeviceDisconnected(), reason: $reason")
        _status.value = when (reason) {
            ConnectionObserver.REASON_NOT_SUPPORTED -> MissingServiceResult(device)
            ConnectionObserver.REASON_LINK_LOSS -> LinkLossResult(device, getData()!!)
            ConnectionObserver.REASON_SUCCESS -> DisconnectedResult(device)
            else -> UnknownErrorResult(device)
        }
    }

    fun setValue(value: T) {
        lastValue = value
        (_status.value as? SuccessResult)?.let {
            _status.value = SuccessResult(it.device, value)
        }
    }
}
