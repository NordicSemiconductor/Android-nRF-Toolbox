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
package no.nordicsemi.android.hts.data

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.common.callback.battery.BatteryLevelResponse
import no.nordicsemi.android.ble.common.callback.ht.TemperatureMeasurementResponse
import no.nordicsemi.android.ble.ktx.asValidResponseFlow
import no.nordicsemi.android.logger.NordicLogger
import no.nordicsemi.android.service.ConnectionObserverAdapter
import java.util.*

val HTS_SERVICE_UUID: UUID = UUID.fromString("00001809-0000-1000-8000-00805f9b34fb")
private val HT_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A1C-0000-1000-8000-00805f9b34fb")

private val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
private val BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

internal class HTSManager internal constructor(
    context: Context,
    private val scope: CoroutineScope,
    private val logger: NordicLogger
) : BleManager(context) {

    private var batteryLevelCharacteristic: BluetoothGattCharacteristic? = null
    private var htCharacteristic: BluetoothGattCharacteristic? = null

    private val data = MutableStateFlow(HTSData())
    val dataHolder = ConnectionObserverAdapter<HTSData>()

    init {
        connectionObserver = dataHolder

        data.onEach {
            dataHolder.setValue(it)
        }.launchIn(scope)
    }

    override fun log(priority: Int, message: String) {
        logger.log(priority, message)
    }

    override fun getMinLogPriority(): Int {
        return Log.VERBOSE
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return HTManagerGattCallback()
    }

    private inner class HTManagerGattCallback : BleManagerGattCallback() {
        override fun initialize() {
            super.initialize()

            setIndicationCallback(htCharacteristic)
                .asValidResponseFlow<TemperatureMeasurementResponse>()
                .onEach {
                    data.tryEmit(data.value.copy(temperatureValue = it.temperature))
                }.launchIn(scope)
            enableIndications(htCharacteristic).enqueue()

            setNotificationCallback(batteryLevelCharacteristic).asValidResponseFlow<BatteryLevelResponse>().onEach {
                data.value = data.value.copy(batteryLevel = it.batteryLevel)
            }.launchIn(scope)
            enableNotifications(batteryLevelCharacteristic).enqueue()
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            gatt.getService(HTS_SERVICE_UUID)?.run {
                htCharacteristic = getCharacteristic(HT_MEASUREMENT_CHARACTERISTIC_UUID)
            }
            gatt.getService(BATTERY_SERVICE_UUID)?.run {
                batteryLevelCharacteristic = getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)
            }
            return htCharacteristic != null
        }

        override fun onServicesInvalidated() {
            htCharacteristic = null
            batteryLevelCharacteristic = null
        }
    }
}
