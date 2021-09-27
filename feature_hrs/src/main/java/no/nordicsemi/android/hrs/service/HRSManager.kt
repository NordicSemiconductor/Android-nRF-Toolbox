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
package no.nordicsemi.android.hrs.service

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import androidx.annotation.IntRange
import no.nordicsemi.android.ble.common.callback.hr.BodySensorLocationDataCallback
import no.nordicsemi.android.ble.common.callback.hr.HeartRateMeasurementDataCallback
import no.nordicsemi.android.ble.common.profile.hr.BodySensorLocation
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.log.LogContract
import no.nordicsemi.android.service.BatteryManager
import java.util.*

/**
 * HRSManager class performs BluetoothGatt operations for connection, service discovery,
 * enabling notification and reading characteristics.
 * All operations required to connect to device with BLE Heart Rate Service and reading
 * heart rate values are performed here.
 */
class HRSManager(context: Context) : BatteryManager<HRSManagerCallbacks>(context) {

    private var heartRateCharacteristic: BluetoothGattCharacteristic? = null
    private var bodySensorLocationCharacteristic: BluetoothGattCharacteristic? = null

    override fun getGattCallback(): BatteryManagerGattCallback {
        return HeartRateManagerCallback()
    }

    /**
     * BluetoothGatt callbacks for connection/disconnection, service discovery,
     * receiving notification, etc.
     */
    private inner class HeartRateManagerCallback : BatteryManagerGattCallback() {
        override fun initialize() {
            super.initialize()
            readCharacteristic(bodySensorLocationCharacteristic)
                .with(object : BodySensorLocationDataCallback() {

                    override fun onDataReceived(device: BluetoothDevice, data: Data) {
                        log(
                            LogContract.Log.Level.APPLICATION,
                            "\"" + BodySensorLocationParser.parse(data) + "\" received"
                        )
                        super.onDataReceived(device, data)
                    }

                    override fun onBodySensorLocationReceived(
                        device: BluetoothDevice,
                        @BodySensorLocation sensorLocation: Int
                    ) {
                        mCallbacks?.onBodySensorLocationReceived(device, sensorLocation)
                    }

                })
                .fail { device: BluetoothDevice?, status: Int ->
                    log(Log.WARN, "Body Sensor Location characteristic not found")
                }
                .enqueue()

            setNotificationCallback(heartRateCharacteristic)
                .with(object : HeartRateMeasurementDataCallback() {

                    override fun onDataReceived(device: BluetoothDevice, data: Data) {
                        log(
                            LogContract.Log.Level.APPLICATION,
                            "\"" + HeartRateMeasurementParser.parse(data) + "\" received"
                        )
                        super.onDataReceived(device, data)
                    }

                    override fun onHeartRateMeasurementReceived(
                        device: BluetoothDevice,
                        @IntRange(from = 0) heartRate: Int,
                        contactDetected: Boolean?,
                        @IntRange(from = 0) energyExpanded: Int?,
                        rrIntervals: List<Int>?
                    ) {
                        mCallbacks?.onHeartRateMeasurementReceived(
                            device,
                            heartRate,
                            contactDetected,
                            energyExpanded,
                            rrIntervals
                        )
                    }
                })
            enableNotifications(heartRateCharacteristic).enqueue()
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(HR_SERVICE_UUID)
            if (service != null) {
                heartRateCharacteristic = service.getCharacteristic(
                    HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID
                )
            }
            return heartRateCharacteristic != null
        }

        override fun isOptionalServiceSupported(gatt: BluetoothGatt): Boolean {
            super.isOptionalServiceSupported(gatt)
            val service = gatt.getService(HR_SERVICE_UUID)
            if (service != null) {
                bodySensorLocationCharacteristic = service.getCharacteristic(
                    BODY_SENSOR_LOCATION_CHARACTERISTIC_UUID
                )
            }
            return bodySensorLocationCharacteristic != null
        }

        override fun onDeviceDisconnected() {
            super.onDeviceDisconnected()
            bodySensorLocationCharacteristic = null
            heartRateCharacteristic = null
        }

        override fun onServicesInvalidated() {}
    }

    companion object {

        val HR_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")
        private val BODY_SENSOR_LOCATION_CHARACTERISTIC_UUID = UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb")
        private val HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")
        private var managerInstance: HRSManager? = null

        /**
         * Singleton implementation of HRSManager class.
         */
        @Synchronized
        fun getInstance(context: Context): HRSManager? {
            if (managerInstance == null) {
                managerInstance = HRSManager(context)
            }
            return managerInstance
        }
    }
}