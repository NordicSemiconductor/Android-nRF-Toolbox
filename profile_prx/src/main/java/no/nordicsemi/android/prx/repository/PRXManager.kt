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
package no.nordicsemi.android.prx.repository

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.common.callback.alert.AlertLevelDataCallback
import no.nordicsemi.android.ble.common.data.alert.AlertLevelData
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.prx.data.PRXRepository
import no.nordicsemi.android.service.BatteryManager
import java.util.*

val LINK_LOSS_SERVICE_UUID = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb")
val PRX_SERVICE_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb")
val ALERT_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A06-0000-1000-8000-00805f9b34fb")

internal class PRXManager(
    context: Context,
    private val scope: CoroutineScope,
    private val dataHolder: PRXRepository
) : BatteryManager(context) {

    private var alertLevelCharacteristic: BluetoothGattCharacteristic? = null
    private var linkLossCharacteristic: BluetoothGattCharacteristic? = null

    private var localAlertLevelCharacteristic: BluetoothGattCharacteristic? = null
    private var linkLossServerCharacteristic: BluetoothGattCharacteristic? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, t->
        Log.e("COROUTINE-EXCEPTION", "Uncaught exception", t)
    }

    var isAlertEnabled = false
        private set

    private inner class ProximityManagerGattCallback : BatteryManagerGattCallback() {
        override fun initialize() {
            super.initialize()

            setWriteCallback(localAlertLevelCharacteristic)
                .with(object : AlertLevelDataCallback() {
                    override fun onAlertLevelChanged(device: BluetoothDevice, level: Int) {
                        dataHolder.setLocalAlarmLevel(level)
                    }
                })

            setWriteCallback(linkLossServerCharacteristic)
                .with(object : AlertLevelDataCallback() {
                    override fun onAlertLevelChanged(device: BluetoothDevice, level: Int) {
                        dataHolder.setLinkLossLevel(level)
                    }
                })

            scope.launch(exceptionHandler) {
                writeCharacteristic(
                    linkLossCharacteristic,
                    AlertLevelData.highAlert(),
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                ).suspend()
            }
        }

        override fun onServerReady(server: BluetoothGattServer) {
            val immediateAlertService = server.getService(PRX_SERVICE_UUID)
            if (immediateAlertService != null) {
                localAlertLevelCharacteristic = immediateAlertService.getCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID)
            }
            val linkLossService = server.getService(LINK_LOSS_SERVICE_UUID)
            if (linkLossService != null) {
                linkLossServerCharacteristic = linkLossService.getCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID)
            }
        }

        override fun onServicesInvalidated() { }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val llService = gatt.getService(LINK_LOSS_SERVICE_UUID)
            if (llService != null) {
                linkLossCharacteristic = llService.getCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID)
            }
            return linkLossCharacteristic != null
        }

        override fun isOptionalServiceSupported(gatt: BluetoothGatt): Boolean {
            super.isOptionalServiceSupported(gatt)
            val iaService = gatt.getService(PRX_SERVICE_UUID)
            if (iaService != null) {
                alertLevelCharacteristic = iaService.getCharacteristic(
                    ALERT_LEVEL_CHARACTERISTIC_UUID
                )
            }
            return alertLevelCharacteristic != null
        }

        override fun onDeviceDisconnected() {
            super.onDeviceDisconnected()
            alertLevelCharacteristic = null
            linkLossCharacteristic = null
            localAlertLevelCharacteristic = null
            linkLossServerCharacteristic = null
            isAlertEnabled = false
        }
    }

    fun writeImmediateAlert(on: Boolean) {
        if (!isConnected) return
        scope.launch(exceptionHandler) {
            writeCharacteristic(
                alertLevelCharacteristic,
                if (on) AlertLevelData.highAlert() else AlertLevelData.noAlert(),
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            ).suspend()

            isAlertEnabled = on
            dataHolder.setRemoteAlarmLevel(on)
        }
    }

    override fun onBatteryLevelChanged(batteryLevel: Int) {
        dataHolder.setBatteryLevel(batteryLevel)
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return ProximityManagerGattCallback()
    }
}
