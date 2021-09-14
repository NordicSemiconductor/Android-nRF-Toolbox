package no.nordicsemi.android.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@SuppressLint("MissingPermission")
internal class NordicBleScanner @Inject constructor(private val bleAdapter: BluetoothAdapter?) {

    val scannerResult = MutableStateFlow<ScanningResult>(DeviceListResult())

    private var isScanning = false

    private val scanner by lazy { bleAdapter?.bluetoothLeScanner }
    private val devices = mutableListOf<BluetoothDevice>()

    private val scanningCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { devices.addIfNotExist(it) }
            scannerResult.value = DeviceListResult(devices)
        }

        override fun onScanFailed(errorCode: Int) {
            scannerResult.value = ScanningErrorResult
        }
    }

    fun getBluetoothStatus(): ScannerStatus {
        return when {
            bleAdapter == null -> ScannerStatus.NOT_AVAILABLE
            bleAdapter.isEnabled -> ScannerStatus.ENABLED
            else -> ScannerStatus.DISABLED
        }
    }

    fun startScanning() {
        if (isScanning) {
            return
        }
        isScanning = true
        scanner?.startScan(scanningCallback)
    }

    fun stopScanning() {
        if (!isScanning) {
            return
        }
        isScanning = false
        scanner?.stopScan(scanningCallback)
    }
}

sealed class ScanningResult

data class DeviceListResult(val devices: List<BluetoothDevice> = emptyList()) : ScanningResult()

object ScanningErrorResult : ScanningResult()

private fun <T> MutableList<T>.addIfNotExist(value: T) {
    if (!contains(value)) {
        add(value)
    }
}
