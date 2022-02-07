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

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.common.callback.hr.BodySensorLocationResponse
import no.nordicsemi.android.ble.common.callback.hr.HeartRateMeasurementResponse
import no.nordicsemi.android.ble.ktx.asValidResponseFlow
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.ble.ktx.suspendForValidResponse
import no.nordicsemi.android.hrs.data.HRSRepository
import no.nordicsemi.android.service.BatteryManager
import java.util.*

val HRS_SERVICE_UUID: UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")
private val BODY_SENSOR_LOCATION_CHARACTERISTIC_UUID = UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb")
private val HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")

internal class HRSManager(
    context: Context,
    scope: CoroutineScope,
    private val dataHolder: HRSRepository
) : BatteryManager(context, scope) {

    private var heartRateCharacteristic: BluetoothGattCharacteristic? = null
    private var bodySensorLocationCharacteristic: BluetoothGattCharacteristic? = null

    override fun onBatteryLevelChanged(batteryLevel: Int) {
        dataHolder.setBatteryLevel(batteryLevel)
    }

    override fun getGattCallback(): BatteryManagerGattCallback {
        return HeartRateManagerCallback()
    }

    private inner class HeartRateManagerCallback : BatteryManagerGattCallback() {
        override fun initialize() {
            super.initialize()

            scope.launch {
                val data = readCharacteristic(bodySensorLocationCharacteristic)
                    .suspendForValidResponse<BodySensorLocationResponse>()

                dataHolder.setSensorLocation(data.sensorLocation)
            }

            setNotificationCallback(heartRateCharacteristic).asValidResponseFlow<HeartRateMeasurementResponse>()
                .onEach {
                    dataHolder.addNewHeartRate(it.heartRate)
                }.launchIn(scope)

            scope.launch {
                enableNotifications(heartRateCharacteristic).suspend()
            }
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(HRS_SERVICE_UUID)
            if (service != null) {
                heartRateCharacteristic = service.getCharacteristic(HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID)
            }
            return heartRateCharacteristic != null
        }

        override fun isOptionalServiceSupported(gatt: BluetoothGatt): Boolean {
            super.isOptionalServiceSupported(gatt)
            val service = gatt.getService(HRS_SERVICE_UUID)
            if (service != null) {
                bodySensorLocationCharacteristic = service.getCharacteristic(BODY_SENSOR_LOCATION_CHARACTERISTIC_UUID)
            }
            return bodySensorLocationCharacteristic != null
        }

        override fun onServicesInvalidated() {
            bodySensorLocationCharacteristic = null
            heartRateCharacteristic = null
        }
    }
}
