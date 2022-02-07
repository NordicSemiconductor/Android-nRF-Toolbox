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
package no.nordicsemi.android.bps.repository

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.common.callback.battery.BatteryLevelResponse
import no.nordicsemi.android.ble.common.callback.bps.BloodPressureMeasurementResponse
import no.nordicsemi.android.ble.common.callback.bps.IntermediateCuffPressureResponse
import no.nordicsemi.android.ble.ktx.asValidResponseFlow
import no.nordicsemi.android.bps.data.BPSData
import no.nordicsemi.android.bps.data.copyWithNewResponse
import no.nordicsemi.android.service.ConnectionObserverAdapter
import java.util.*

val BPS_SERVICE_UUID: UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb")
private val BPM_CHARACTERISTIC_UUID = UUID.fromString("00002A35-0000-1000-8000-00805f9b34fb")
private val ICP_CHARACTERISTIC_UUID = UUID.fromString("00002A36-0000-1000-8000-00805f9b34fb")

private val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
private val BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

internal class BPSManager(
    @ApplicationContext context: Context,
    private val scope: CoroutineScope
) : BleManager(context) {

    private var batteryLevelCharacteristic: BluetoothGattCharacteristic? = null
    private var bpmCharacteristic: BluetoothGattCharacteristic? = null
    private var icpCharacteristic: BluetoothGattCharacteristic? = null

    private val data = MutableStateFlow(BPSData())
    val dataHolder = ConnectionObserverAdapter<BPSData>()

    init {
        setConnectionObserver(dataHolder)

        data.onEach {
            dataHolder.setValue(it)
        }.launchIn(scope)
    }

    override fun getMinLogPriority(): Int {
        return Log.VERBOSE
    }

    override fun log(priority: Int, message: String) {
        Log.println(priority, "AAA", message)
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return BloodPressureManagerGattCallback()
    }

    private inner class BloodPressureManagerGattCallback : BleManagerGattCallback() {

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun initialize() {
            super.initialize()

            setNotificationCallback(icpCharacteristic).asValidResponseFlow<IntermediateCuffPressureResponse>()
                .onEach { data.tryEmit(data.value.copyWithNewResponse(it)) }
                .launchIn(scope)

            setIndicationCallback(bpmCharacteristic).asValidResponseFlow<BloodPressureMeasurementResponse>()
                .onEach { data.tryEmit(data.value.copyWithNewResponse(it)) }
                .launchIn(scope)

            setNotificationCallback(batteryLevelCharacteristic).asValidResponseFlow<BatteryLevelResponse>()
                .onEach {
                    data.value = data.value.copy(batteryLevel = it.batteryLevel)
                }.launchIn(scope)

            enableNotifications(icpCharacteristic).enqueue()
            enableIndications(bpmCharacteristic).enqueue()
            enableNotifications(batteryLevelCharacteristic).enqueue()
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            gatt.getService(BPS_SERVICE_UUID)?.run {
                bpmCharacteristic = getCharacteristic(BPM_CHARACTERISTIC_UUID)
                icpCharacteristic = getCharacteristic(ICP_CHARACTERISTIC_UUID)
            }
            gatt.getService(BATTERY_SERVICE_UUID)?.run {
                batteryLevelCharacteristic = getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)
            }
            return bpmCharacteristic != null && batteryLevelCharacteristic != null
        }

        override fun isOptionalServiceSupported(gatt: BluetoothGatt): Boolean {
            super.isOptionalServiceSupported(gatt) // ignore the result of this
            return icpCharacteristic != null
        }

        override fun onServicesInvalidated() {
            icpCharacteristic = null
            bpmCharacteristic = null
            batteryLevelCharacteristic = null
        }
    }
}
