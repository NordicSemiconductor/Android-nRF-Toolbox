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
package no.nordicsemi.android.csc.service

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import androidx.annotation.FloatRange
import no.nordicsemi.android.ble.common.callback.csc.CyclingSpeedAndCadenceMeasurementDataCallback
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.csc.batery.BatteryManager
import no.nordicsemi.android.csc.batery.CSCMeasurementParser.parse
import no.nordicsemi.android.log.LogContract
import java.util.*

private const val SETTINGS_WHEEL_SIZE_DEFAULT = 2340

internal class CSCManager(context: Context) : BatteryManager<CSCManagerCallbacks?>(context) {

    private var cscMeasurementCharacteristic: BluetoothGattCharacteristic? = null
    private var wheelSize = SETTINGS_WHEEL_SIZE_DEFAULT

    override fun getGattCallback(): BatteryManagerGattCallback {
        return CSCManagerGattCallback()
    }

    fun setWheelSize(value: Int) {
        wheelSize = value
    }

    /**
     * BluetoothGatt callbacks for connection/disconnection, service discovery,
     * receiving indication, etc.
     */
    private inner class CSCManagerGattCallback : BatteryManagerGattCallback() {
        override fun initialize() {
            super.initialize()

            // CSC characteristic is required
            setNotificationCallback(cscMeasurementCharacteristic)
                .with(object : CyclingSpeedAndCadenceMeasurementDataCallback() {
                    override fun onDataReceived(device: BluetoothDevice, data: Data) {
                        log(LogContract.Log.Level.APPLICATION, "\"" + parse(data) + "\" received")

                        // Pass through received data
                        super.onDataReceived(device, data)
                    }

                    override fun getWheelCircumference(): Float {
                        return wheelSize.toFloat()
                    }

                    override fun onDistanceChanged(
                        device: BluetoothDevice,
                        @FloatRange(from = 0.0) totalDistance: Float,
                        @FloatRange(from = 0.0) distance: Float,
                        @FloatRange(from = 0.0) speed: Float
                    ) {
                        mCallbacks!!.onDistanceChanged(device, totalDistance, distance, speed)
                    }

                    override fun onCrankDataChanged(
                        device: BluetoothDevice,
                        @FloatRange(from = 0.0) crankCadence: Float,
                        gearRatio: Float
                    ) {
                        mCallbacks!!.onCrankDataChanged(device, crankCadence, gearRatio)
                    }

                    override fun onInvalidDataReceived(
                        device: BluetoothDevice,
                        data: Data
                    ) {
                        log(Log.WARN, "Invalid CSC Measurement data received: $data")
                    }
                })
            enableNotifications(cscMeasurementCharacteristic).enqueue()
        }

        public override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(CYCLING_SPEED_AND_CADENCE_SERVICE_UUID)
            if (service != null) {
                cscMeasurementCharacteristic = service.getCharacteristic(
                    CSC_MEASUREMENT_CHARACTERISTIC_UUID
                )
            }
            return true
        }

        override fun onDeviceDisconnected() {
            super.onDeviceDisconnected()
            cscMeasurementCharacteristic = null
        }

        override fun onServicesInvalidated() {}
    }

    companion object {
        /** Cycling Speed and Cadence service UUID.  */
        val CYCLING_SPEED_AND_CADENCE_SERVICE_UUID =
            UUID.fromString("00001816-0000-1000-8000-00805f9b34fb")

        /** Cycling Speed and Cadence Measurement characteristic UUID.  */
        private val CSC_MEASUREMENT_CHARACTERISTIC_UUID =
            UUID.fromString("00002A5B-0000-1000-8000-00805f9b34fb")
    }
}
