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
package no.nordicsemi.android.csc.repository

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.common.callback.battery.BatteryLevelResponse
import no.nordicsemi.android.ble.common.callback.csc.CyclingSpeedAndCadenceMeasurementResponse
import no.nordicsemi.android.ble.ktx.asValidResponseFlow
import no.nordicsemi.android.csc.data.CSCData
import no.nordicsemi.android.csc.data.WheelSize
import no.nordicsemi.android.service.ConnectionObserverAdapter
import java.util.*

val CSC_SERVICE_UUID: UUID = UUID.fromString("00001816-0000-1000-8000-00805f9b34fb")
private val CSC_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A5B-0000-1000-8000-00805f9b34fb")

private val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
private val BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

internal class CSCManager(
    context: Context,
    private val scope: CoroutineScope,
) : BleManager(context) {

    private var batteryLevelCharacteristic: BluetoothGattCharacteristic? = null
    private var cscMeasurementCharacteristic: BluetoothGattCharacteristic? = null
    private var wheelSize: WheelSize = WheelSize()

    private var previousResponse: CyclingSpeedAndCadenceMeasurementResponse? = null

    private val data = MutableStateFlow(CSCData())
    val dataHolder = ConnectionObserverAdapter<CSCData>()

    init {
        setConnectionObserver(dataHolder)

        data.onEach {
            dataHolder.setValue(it)
        }.launchIn(scope)
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return CSCManagerGattCallback()
    }

    fun setWheelSize(value: WheelSize) {
        wheelSize = value
    }

    private inner class CSCManagerGattCallback : BleManagerGattCallback() {
        override fun initialize() {
            super.initialize()

            setNotificationCallback(cscMeasurementCharacteristic).asValidResponseFlow<CyclingSpeedAndCadenceMeasurementResponse>()
                .onEach {
                    previousResponse?.let { previousResponse ->
                        val wheelCircumference = wheelSize.value.toFloat()
                        val totalDistance = it.getTotalDistance(wheelSize.value.toFloat())
                        val distance = it.getDistance(wheelCircumference, previousResponse)
                        val speed = it.getSpeed(wheelCircumference, previousResponse)
                        val crankCadence = it.getCrankCadence(previousResponse)
                        val gearRatio = it.getGearRatio(previousResponse)

                        data.tryEmit(data.value.copy(
                            totalDistance = totalDistance,
                            distance = distance,
                            speed = speed,
                            wheelSize = wheelSize,
                            cadence = crankCadence,
                            gearRatio = gearRatio,
                        ))
                    }

                    previousResponse = it
                }.launchIn(scope)
            enableNotifications(cscMeasurementCharacteristic).enqueue()

            setNotificationCallback(batteryLevelCharacteristic).asValidResponseFlow<BatteryLevelResponse>().onEach {
                data.value = data.value.copy(batteryLevel = it.batteryLevel)
            }.launchIn(scope)
            enableNotifications(batteryLevelCharacteristic).enqueue()
        }

        public override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            gatt.getService(CSC_SERVICE_UUID)?.run {
                cscMeasurementCharacteristic = getCharacteristic(CSC_MEASUREMENT_CHARACTERISTIC_UUID)
            }
            gatt.getService(BATTERY_SERVICE_UUID)?.run {
                batteryLevelCharacteristic = getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)
            }
            return cscMeasurementCharacteristic != null && batteryLevelCharacteristic != null
        }

        override fun onServicesInvalidated() {
            cscMeasurementCharacteristic = null
            batteryLevelCharacteristic = null
        }
    }
}
