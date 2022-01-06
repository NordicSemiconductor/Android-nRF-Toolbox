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
package no.nordicsemi.android.rscs.service

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import no.nordicsemi.android.ble.common.callback.rsc.RunningSpeedAndCadenceMeasurementDataCallback
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.log.LogContract
import no.nordicsemi.android.rscs.data.RSCSRepository
import no.nordicsemi.android.service.BatteryManager
import java.util.*

/** Running Speed and Cadence Measurement service UUID  */
val RSCS_SERVICE_UUID: UUID = UUID.fromString("00001814-0000-1000-8000-00805F9B34FB")

/** Running Speed and Cadence Measurement characteristic UUID  */
private val RSC_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A53-0000-1000-8000-00805F9B34FB")

internal class RSCSManager internal constructor(
    context: Context,
    private val dataHolder: RSCSRepository
) : BatteryManager(context) {

    private var rscMeasurementCharacteristic: BluetoothGattCharacteristic? = null

    private val callback = object : RunningSpeedAndCadenceMeasurementDataCallback() {
        override fun onDataReceived(device: BluetoothDevice, data: Data) {
            log(
                LogContract.Log.Level.APPLICATION,
                "\"" + RSCMeasurementParser.parse(data).toString() + "\" received"
            )
            super.onDataReceived(device, data)
        }

        override fun onRSCMeasurementReceived(
            device: BluetoothDevice,
            running: Boolean,
            instantaneousSpeed: Float,
            instantaneousCadence: Int,
            strideLength: Int?,
            totalDistance: Long?
        ) {
            dataHolder.setNewData(running, instantaneousSpeed, instantaneousCadence, strideLength, totalDistance)
        }
    }

    override fun onBatteryLevelChanged(batteryLevel: Int) {
        dataHolder.setBatteryLevel(batteryLevel)
    }

    override fun getGattCallback(): BatteryManagerGattCallback {
        return RSCManagerGattCallback()
    }

    /**
     * BluetoothGatt callbacks for connection/disconnection, service discovery,
     * receiving indication, etc.
     */
    private inner class RSCManagerGattCallback : BatteryManagerGattCallback() {

        override fun initialize() {
            super.initialize()
            setNotificationCallback(rscMeasurementCharacteristic)
                .with(callback)
            enableNotifications(rscMeasurementCharacteristic).enqueue()
        }

        public override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(RSCS_SERVICE_UUID)
            if (service != null) {
                rscMeasurementCharacteristic = service.getCharacteristic(
                    RSC_MEASUREMENT_CHARACTERISTIC_UUID
                )
            }
            return rscMeasurementCharacteristic != null
        }

        override fun onServicesInvalidated() {

        }

        override fun onDeviceDisconnected() {
            super.onDeviceDisconnected()
            rscMeasurementCharacteristic = null
        }
    }
}
