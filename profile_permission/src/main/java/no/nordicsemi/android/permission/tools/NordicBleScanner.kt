package no.nordicsemi.android.permission.tools

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import javax.inject.Inject

@SuppressLint("MissingPermission")
class NordicBleScanner @Inject constructor(private val bleAdapter: BluetoothAdapter?) {

    fun getBluetoothStatus(): ScannerStatus {
        return when {
            bleAdapter == null -> ScannerStatus.NOT_AVAILABLE
            bleAdapter.isEnabled -> ScannerStatus.ENABLED
            else -> ScannerStatus.DISABLED
        }
    }
}
