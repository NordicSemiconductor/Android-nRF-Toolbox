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
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.common.callback.bps.BloodPressureMeasurementResponse
import no.nordicsemi.android.ble.common.callback.bps.IntermediateCuffPressureResponse
import no.nordicsemi.android.ble.ktx.asValidResponseFlow
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.bps.data.BPSRepository
import no.nordicsemi.android.log.Logger
import no.nordicsemi.android.service.BatteryManager
import no.nordicsemi.android.service.CloseableCoroutineScope
import java.util.*
import javax.inject.Inject

val BPS_SERVICE_UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb")

private val BPM_CHARACTERISTIC_UUID = UUID.fromString("00002A35-0000-1000-8000-00805f9b34fb")

private val ICP_CHARACTERISTIC_UUID = UUID.fromString("00002A36-0000-1000-8000-00805f9b34fb")

@ViewModelScoped
internal class BPSManager @Inject constructor(
    @ApplicationContext context: Context,
    private val dataHolder: BPSRepository
) : BatteryManager(context) {

    private val scope = CloseableCoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var bpmCharacteristic: BluetoothGattCharacteristic? = null
    private var icpCharacteristic: BluetoothGattCharacteristic? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, t->
        Log.e("COROUTINE-EXCEPTION", "Uncaught exception", t)
    }

    override fun onBatteryLevelChanged(batteryLevel: Int) {
        dataHolder.setBatteryLevel(batteryLevel)
    }

    private inner class BloodPressureManagerGattCallback : BatteryManagerGattCallback() {

        override fun initialize() {
            super.initialize()

            setNotificationCallback(icpCharacteristic).asValidResponseFlow<IntermediateCuffPressureResponse>()
                .onEach {
                    dataHolder.setIntermediateCuffPressure(
                        it.cuffPressure,
                        it.unit,
                        it.pulseRate,
                        it.userID,
                        it.status,
                        it.timestamp
                    )
                }.launchIn(scope)

            setIndicationCallback(bpmCharacteristic).asValidResponseFlow<BloodPressureMeasurementResponse>()
                .onEach {
                    dataHolder.setBloodPressureMeasurement(
                        it.systolic,
                        it.diastolic,
                        it.meanArterialPressure,
                        it.unit,
                        it.pulseRate,
                        it.userID,
                        it.status,
                        it.timestamp
                    )
                }.launchIn(scope)

            scope.launch(exceptionHandler) {
                enableNotifications(icpCharacteristic).suspend()
            }

            scope.launch(exceptionHandler) {
                enableIndications(bpmCharacteristic).suspend()
            }
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(BPS_SERVICE_UUID)
            if (service != null) {
                bpmCharacteristic = service.getCharacteristic(BPM_CHARACTERISTIC_UUID)
                icpCharacteristic = service.getCharacteristic(ICP_CHARACTERISTIC_UUID)
            }
            return bpmCharacteristic != null
        }

        override fun onServicesInvalidated() {}

        override fun isOptionalServiceSupported(gatt: BluetoothGatt): Boolean {
            super.isOptionalServiceSupported(gatt) // ignore the result of this
            return icpCharacteristic != null
        }

        override fun onDeviceDisconnected() {
            icpCharacteristic = null
            bpmCharacteristic = null
        }
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return BloodPressureManagerGattCallback()
    }

    fun release() {
        scope.close()
    }
}
