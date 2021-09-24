package no.nordicsemi.android.scanner.tools

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@SuppressLint("MissingPermission")
internal class NordicBleScanner @Inject constructor(private val bleAdapter: BluetoothAdapter?) {

    val scannerResult = MutableStateFlow<ScanningResult>(DeviceListResult())

    fun getBluetoothStatus(): ScannerStatus {
        return when {
            bleAdapter == null -> ScannerStatus.NOT_AVAILABLE
            bleAdapter.isEnabled -> ScannerStatus.ENABLED
            else -> ScannerStatus.DISABLED
        }
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
