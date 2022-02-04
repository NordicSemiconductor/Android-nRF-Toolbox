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

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.common.callback.battery.BatteryLevelResponse
import no.nordicsemi.android.ble.common.callback.csc.CyclingSpeedAndCadenceMeasurementResponse
import no.nordicsemi.android.ble.ktx.asValidResponseFlow
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.csc.data.CSCData
import no.nordicsemi.android.csc.data.WheelSize
import no.nordicsemi.android.service.*
import java.util.*
import javax.inject.Inject

val CSC_SERVICE_UUID: UUID = UUID.fromString("00001816-0000-1000-8000-00805f9b34fb")
private val CSC_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A5B-0000-1000-8000-00805f9b34fb")

private val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
private val BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

internal class CSCRepo @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {

    suspend fun downloadData(device: BluetoothDevice) = callbackFlow<BleManagerResult<CSCData>> {
        val scope = CoroutineScope(coroutineContext)
        val manager = CSCManager(context, scope)

        manager.dataHolder.status.onEach {
            trySend(it)
        }.launchIn(scope)

        scope.launch {
            manager.connect(device)
                .useAutoConnect(false)
                .retry(3, 100)
                .suspend()
        }

        awaitClose {
            scope.launch {
                manager.disconnect().suspend()
            }
            scope.cancel()
        }
    }
}

internal class CSCManager(
    context: Context,
    scope: CoroutineScope,
//    private val channel: SendChannel<BleManagerResult<CSCData>>
) : BatteryManager(context, scope) {

    private var batteryLevelCharacteristic: BluetoothGattCharacteristic? = null
    private var cscMeasurementCharacteristic: BluetoothGattCharacteristic? = null
    private var wheelSize: WheelSize = WheelSize()

    private var previousResponse: CyclingSpeedAndCadenceMeasurementResponse? = null

    private val data = MutableStateFlow(CSCData())
    val dataHolder = ConnectionObserverAdapter<CSCData>()

    private val exceptionHandler = CoroutineExceptionHandler { context, t ->
        Log.e("COROUTINE-EXCEPTION", "Uncaught exception", t)
    }

    init {
        setConnectionObserver(dataHolder)

        data.onEach {
            dataHolder.setValue(it)
        }.launchIn(scope)
    }

    override fun getGattCallback(): BatteryManagerGattCallback {
        return CSCManagerGattCallback()
    }

    fun setWheelSize(value: WheelSize) {
        wheelSize = value
    }

    private inner class CSCManagerGattCallback : BatteryManagerGattCallback() {
        override fun initialize() {
            super.initialize()

            setNotificationCallback(cscMeasurementCharacteristic).asValidResponseFlow<CyclingSpeedAndCadenceMeasurementResponse>()
                .onEach {
                    previousResponse?.let { previousResponse ->
                        val wheelCircumference = wheelSize.value.toFloat()
                        val totalDistance = it.getTotalDistance(wheelSize.value.toFloat())
                        val distance = it.getDistance(wheelCircumference, previousResponse)
                        val speed = it.getSpeed(wheelCircumference, previousResponse)

                        //todo
                        data.value.copy(totalDistance, )
                        repository.setNewDistance(totalDistance, distance, speed, wheelSize)

                        val crankCadence = it.getCrankCadence(previousResponse)
                        val gearRatio = it.getGearRatio(previousResponse)
                        repository.setNewCrankCadence(crankCadence, gearRatio, wheelSize)
                    }

                    previousResponse = it
                }.launchIn(scope)

            scope.launch(exceptionHandler) {
                enableNotifications(cscMeasurementCharacteristic).suspend()
            }

            setNotificationCallback(batteryLevelCharacteristic).asValidResponseFlow<BatteryLevelResponse>().onEach {
                data.value = data.value.copy(batteryLevel = it.batteryLevel)
            }.launchIn(scope)

            scope.launch {
                enableNotifications(batteryLevelCharacteristic).suspend()
            }
        }

        public override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(CSC_SERVICE_UUID)
            if (service != null) {
                cscMeasurementCharacteristic = service.getCharacteristic(CSC_MEASUREMENT_CHARACTERISTIC_UUID)
                batteryLevelCharacteristic = service.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)
            }
            return cscMeasurementCharacteristic != null
        }

        override fun onServicesInvalidated() {
            super.onServicesInvalidated()
            cscMeasurementCharacteristic = null
            batteryLevelCharacteristic = null
        }
    }
}
