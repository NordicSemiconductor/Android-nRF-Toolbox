package no.nordicsemi.android.service

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import no.nordicsemi.android.ble.BleManager
import java.util.*

private val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
private val BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

abstract class BatteryManager(
    context: Context,
    protected val scope: CoroutineScope,
) : BleManager(context) {

    private val TAG = "BLE-MANAGER"

    private var batteryLevelCharacteristic: BluetoothGattCharacteristic? = null




    fun enableBatteryLevelCharacteristicNotifications() {
        if (isConnected) {

        }
    }

    override fun log(priority: Int, message: String) {
        super.log(priority, message)
        Log.println(priority, TAG, message)
    }

    protected abstract inner class BatteryManagerGattCallback : BleManagerGattCallback() {
        override fun initialize() {
            enableBatteryLevelCharacteristicNotifications()
        }

        override fun isOptionalServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(BATTERY_SERVICE_UUID)
            if (service != null) {
                batteryLevelCharacteristic = service.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)
            }
            return batteryLevelCharacteristic != null
        }

        override fun onServicesInvalidated() {
            batteryLevelCharacteristic = null
        }
    }

    fun releaseScope() {
        scope.cancel()
    }
}
