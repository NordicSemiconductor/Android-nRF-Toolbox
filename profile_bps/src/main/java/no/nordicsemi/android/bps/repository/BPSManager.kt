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

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.ble.common.callback.bps.BloodPressureMeasurementDataCallback
import no.nordicsemi.android.ble.common.callback.bps.IntermediateCuffPressureDataCallback
import no.nordicsemi.android.ble.common.profile.bp.BloodPressureTypes
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.bps.data.BPSRepository
import no.nordicsemi.android.service.BatteryManager
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/** Blood Pressure service UUID.  */
val BPS_SERVICE_UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb")

/** Blood Pressure Measurement characteristic UUID.  */
private val BPM_CHARACTERISTIC_UUID = UUID.fromString("00002A35-0000-1000-8000-00805f9b34fb")

/** Intermediate Cuff Pressure characteristic UUID.  */
private val ICP_CHARACTERISTIC_UUID = UUID.fromString("00002A36-0000-1000-8000-00805f9b34fb")

@Singleton
internal class BPSManager @Inject constructor(
    @ApplicationContext context: Context,
    private val dataHolder: BPSRepository
) : BatteryManager(context) {

    private var bpmCharacteristic: BluetoothGattCharacteristic? = null
    private var icpCharacteristic: BluetoothGattCharacteristic? = null

    private val intermediateCuffPressureCallback = object : IntermediateCuffPressureDataCallback() {

        override fun onIntermediateCuffPressureReceived(
            device: BluetoothDevice,
            cuffPressure: Float,
            unit: Int,
            pulseRate: Float?,
            userID: Int?,
            status: BloodPressureTypes.BPMStatus?,
            calendar: Calendar?
        ) {
            dataHolder.setIntermediateCuffPressure(
                cuffPressure,
                unit,
                pulseRate,
                userID,
                status,
                calendar
            )
        }

        override fun onInvalidDataReceived(device: BluetoothDevice, data: Data) {
            log(Log.WARN, "Invalid ICP data received: $data")
        }
    }

    private val bloodPressureMeasurementDataCallback = object : BloodPressureMeasurementDataCallback() {

        override fun onBloodPressureMeasurementReceived(
            device: BluetoothDevice,
            systolic: Float,
            diastolic: Float,
            meanArterialPressure: Float,
            unit: Int,
            pulseRate: Float?,
            userID: Int?,
            status: BloodPressureTypes.BPMStatus?,
            calendar: Calendar?
        ) {
            dataHolder.setBloodPressureMeasurement(
                systolic,
                diastolic,
                meanArterialPressure,
                unit,
                pulseRate,
                userID,
                status,
                calendar
            )
        }

        override fun onInvalidDataReceived(device: BluetoothDevice, data: Data) {
            log(Log.WARN, "Invalid BPM data received: $data")
        }
    }

    override fun onBatteryLevelChanged(batteryLevel: Int) {
        dataHolder.setBatteryLevel(batteryLevel)
    }

    private inner class BloodPressureManagerGattCallback : BatteryManagerGattCallback() {

        override fun initialize() {
            super.initialize()
            setNotificationCallback(icpCharacteristic)
                .with(intermediateCuffPressureCallback)
            setIndicationCallback(bpmCharacteristic)
                .with(bloodPressureMeasurementDataCallback)
            enableNotifications(icpCharacteristic)
                .fail { device, status ->
                    log(
                        Log.WARN,
                        "Intermediate Cuff Pressure characteristic not found"
                    )
                }
                .enqueue()
            enableIndications(bpmCharacteristic).enqueue()
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(BPS_SERVICE_UUID)
            if (service != null) {
                bpmCharacteristic = service.getCharacteristic(BPM_CHARACTERISTIC_UUID)
                icpCharacteristic = service.getCharacteristic(ICP_CHARACTERISTIC_UUID)
            }
            return bpmCharacteristic != null
        }

        override fun onServicesInvalidated() { }

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
}
