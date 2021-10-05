/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.nordicsemi.android.hts.service

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import no.nordicsemi.android.ble.common.callback.ht.TemperatureMeasurementDataCallback
import no.nordicsemi.android.ble.common.profile.ht.TemperatureType
import no.nordicsemi.android.ble.common.profile.ht.TemperatureUnit
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.hts.data.HTSDataHolder
import no.nordicsemi.android.log.LogContract
import no.nordicsemi.android.service.BatteryManager
import java.util.*

private val HT_SERVICE_UUID = UUID.fromString("00001809-0000-1000-8000-00805f9b34fb")
private val HT_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A1C-0000-1000-8000-00805f9b34fb")

/**
 * [HTSManager] class performs [BluetoothGatt] operations for connection, service discovery,
 * enabling indication and reading characteristics. All operations required to connect to device
 * with BLE HT Service and reading health thermometer values are performed here.
 */
class HTSManager internal constructor(context: Context, private val dataHolder: HTSDataHolder) : BatteryManager(context) {

    private var htCharacteristic: BluetoothGattCharacteristic? = null

    private val temperatureMeasurementDataCallback = object : TemperatureMeasurementDataCallback() {
        override fun onDataReceived(device: BluetoothDevice, data: Data) {
            log(
                LogContract.Log.Level.APPLICATION,
                "\"" + TemperatureMeasurementParser.parse(data) + "\" received"
            )
            super.onDataReceived(device, data)
        }

        override fun onTemperatureMeasurementReceived(
            device: BluetoothDevice,
            temperature: Float,
            @TemperatureUnit unit: Int,
            calendar: Calendar?,
            @TemperatureType type: Int?
        ) {
            dataHolder.setNewTemperature(temperature)
        }
    }

    override fun onBatteryLevelChanged(batteryLevel: Int) {
        dataHolder.setBatteryLevel(batteryLevel)
    }

    override fun getGattCallback(): BatteryManagerGattCallback {
        return HTManagerGattCallback()
    }

    /**
     * BluetoothGatt callbacks for connection/disconnection, service discovery,
     * receiving indication, etc..
     */
    private inner class HTManagerGattCallback : BatteryManagerGattCallback() {
        override fun initialize() {
            super.initialize()
            setIndicationCallback(htCharacteristic)
                .with(temperatureMeasurementDataCallback)
            enableIndications(htCharacteristic).enqueue()
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(HT_SERVICE_UUID)
            if (service != null) {
                htCharacteristic = service.getCharacteristic(HT_MEASUREMENT_CHARACTERISTIC_UUID)
            }
            return htCharacteristic != null
        }

        override fun onDeviceDisconnected() {
            super.onDeviceDisconnected()
            htCharacteristic = null
        }

        override fun onServicesInvalidated() {}
    }
}
