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
package no.nordicsemi.android.hts.repository

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.common.callback.ht.TemperatureMeasurementResponse
import no.nordicsemi.android.ble.ktx.asValidResponseFlow
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.hts.data.HTSRepository
import no.nordicsemi.android.service.BatteryManager
import java.util.*

val HTS_SERVICE_UUID: UUID = UUID.fromString("00001809-0000-1000-8000-00805f9b34fb")
private val HT_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A1C-0000-1000-8000-00805f9b34fb")

/**
 * [HTSManager] class performs [BluetoothGatt] operations for connection, service discovery,
 * enabling indication and reading characteristics. All operations required to connect to device
 * with BLE HT Service and reading health thermometer values are performed here.
 */
internal class HTSManager internal constructor(
    context: Context,
    private val scope: CoroutineScope,
    private val dataHolder: HTSRepository
) : BatteryManager(context) {

    private var htCharacteristic: BluetoothGattCharacteristic? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, t->
        Log.e("COROUTINE-EXCEPTION", "Uncaught exception", t)
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
                .asValidResponseFlow<TemperatureMeasurementResponse>()
                .onEach {
                    dataHolder.setNewTemperature(it.temperature)
                }.launchIn(scope)

            scope.launch(exceptionHandler) {
                enableIndications(htCharacteristic).suspend()
            }
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(HTS_SERVICE_UUID)
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
