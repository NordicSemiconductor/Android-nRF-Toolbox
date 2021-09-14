package no.nordicsemi.android.csc.batery

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import androidx.annotation.IntRange
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.common.callback.battery.BatteryLevelDataCallback
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.csc.service.BatteryManagerCallbacks
import no.nordicsemi.android.log.LogContract
import java.util.*

/**
 * The Ble Manager with Battery Service support.
 *
 * @param <T> The profile callbacks type.
 * @see BleManager
</T> */
abstract class BatteryManager<T : BatteryManagerCallbacks?>(context: Context) : LoggableBleManager<T>(context) {

    private var batteryLevelCharacteristic: BluetoothGattCharacteristic? = null
    /**
     * Returns the last received Battery Level value.
     * The value is set to null when the device disconnects.
     * @return Battery Level value, in percent.
     */
    /** Last received Battery Level value.  */
    var batteryLevel: Int? = null
        private set
    private val batteryLevelDataCallback: DataReceivedCallback =
        object : BatteryLevelDataCallback() {
            override fun onBatteryLevelChanged(
                device: BluetoothDevice,
                @IntRange(from = 0, to = 100) batteryLevel: Int
            ) {
                log(LogContract.Log.Level.APPLICATION, "Battery Level received: $batteryLevel%")
                this@BatteryManager.batteryLevel = batteryLevel
                mCallbacks!!.onBatteryLevelChanged(device, batteryLevel)
            }

            override fun onInvalidDataReceived(device: BluetoothDevice, data: Data) {
                log(Log.WARN, "Invalid Battery Level data received: $data")
            }
        }

    fun readBatteryLevelCharacteristic() {
        if (isConnected) {
            readCharacteristic(batteryLevelCharacteristic)
                .with(batteryLevelDataCallback)
                .fail { device: BluetoothDevice?, status: Int ->
                    log(
                        Log.WARN,
                        "Battery Level characteristic not found"
                    )
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
                    log(
                        Log.INFO,
                        "Battery Level notifications enabled"
                    )
                }
                .fail { device: BluetoothDevice?, status: Int ->
                    log(
                        Log.WARN,
                        "Battery Level characteristic not found"
                    )
                }
                .enqueue()
        }
    }

    /**
     * Disables Battery Level notifications on the Server.
     */
    fun disableBatteryLevelCharacteristicNotifications() {
        if (isConnected) {
            disableNotifications(batteryLevelCharacteristic)
                .done { device: BluetoothDevice? ->
                    log(
                        Log.INFO,
                        "Battery Level notifications disabled"
                    )
                }
                .enqueue()
        }
    }

    protected abstract inner class BatteryManagerGattCallback : BleManagerGattCallback() {
        override fun initialize() {
            readBatteryLevelCharacteristic()
            enableBatteryLevelCharacteristicNotifications()
        }

        override fun isOptionalServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(BATTERY_SERVICE_UUID)
            if (service != null) {
                batteryLevelCharacteristic = service.getCharacteristic(
                    BATTERY_LEVEL_CHARACTERISTIC_UUID
                )
            }
            return batteryLevelCharacteristic != null
        }

        override fun onDeviceDisconnected() {
            batteryLevelCharacteristic = null
            batteryLevel = null
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
