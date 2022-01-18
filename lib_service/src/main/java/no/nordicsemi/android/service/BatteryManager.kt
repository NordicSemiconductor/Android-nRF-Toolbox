package no.nordicsemi.android.service

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import androidx.annotation.IntRange
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.common.callback.battery.BatteryLevelDataCallback
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.log.LogContract
import java.util.*

/**
 * The Ble Manager with Battery Service support.
 *
 * @param <T> The profile callbacks type.
 * @see BleManager
</T> */
abstract class BatteryManager(context: Context) : BleManager(context) {

    private val TAG = "BLE-MANAGER"

    private var batteryLevelCharacteristic: BluetoothGattCharacteristic? = null

    private val batteryLevelDataCallback: DataReceivedCallback =
        object : BatteryLevelDataCallback() {
            override fun onBatteryLevelChanged(
                device: BluetoothDevice,
                @IntRange(from = 0, to = 100) batteryLevel: Int
            ) {
                log(LogContract.Log.Level.APPLICATION, "Battery Level received: $batteryLevel%")
                onBatteryLevelChanged(batteryLevel)
            }

            override fun onInvalidDataReceived(device: BluetoothDevice, data: Data) {
                log(Log.WARN, "Invalid Battery Level data received: $data")
            }
        }

    protected abstract fun onBatteryLevelChanged(batteryLevel: Int)

    fun readBatteryLevelCharacteristic() {
        if (isConnected) {
            readCharacteristic(batteryLevelCharacteristic)
                .with(batteryLevelDataCallback)
                .fail { device: BluetoothDevice?, status: Int ->
                    log(Log.WARN, "Battery Level characteristic not found")
                }
                .enqueue()
        }
    }

    fun enableBatteryLevelCharacteristicNotifications() {
        if (isConnected) {
            // If the Battery Level characteristic is null, the request will be ignored
            setNotificationCallback(batteryLevelCharacteristic)
                .with(batteryLevelDataCallback)
            enableNotifications(batteryLevelCharacteristic)
                .done { device: BluetoothDevice? ->
                    log(Log.INFO, "Battery Level notifications enabled")
                }
                .fail { device: BluetoothDevice?, status: Int ->
                    log(Log.WARN, "Battery Level characteristic not found")
                }
                .enqueue()
        }
    }

    override fun log(priority: Int, message: String) {
        super.log(priority, message)
        Log.println(priority, TAG, message)
    }

    protected abstract inner class BatteryManagerGattCallback : BleManagerGattCallback() {
        override fun initialize() {
            readBatteryLevelCharacteristic()
            enableBatteryLevelCharacteristicNotifications()
        }

        override fun isOptionalServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(BATTERY_SERVICE_UUID)
            if (service != null) {
                batteryLevelCharacteristic = service.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)
            }
            return batteryLevelCharacteristic != null
        }

        override fun onDeviceDisconnected() {
            batteryLevelCharacteristic = null
            onBatteryLevelChanged(0)
        }
    }

    companion object {
        /** Battery Service UUID.  */
        private val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")

        /** Battery Level characteristic UUID.  */
        private val BATTERY_LEVEL_CHARACTERISTIC_UUID =
            UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")
    }
}
