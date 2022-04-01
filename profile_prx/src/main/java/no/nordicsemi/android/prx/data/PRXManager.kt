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
package no.nordicsemi.android.prx.data

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.common.callback.alert.AlertLevelDataCallback
import no.nordicsemi.android.ble.common.callback.battery.BatteryLevelResponse
import no.nordicsemi.android.ble.common.data.alert.AlertLevelData
import no.nordicsemi.android.ble.ktx.asValidResponseFlow
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.logger.ToolboxLogger
import no.nordicsemi.android.service.ConnectionObserverAdapter
import no.nordicsemi.android.utils.launchWithCatch
import java.util.*

val PRX_SERVICE_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb")
val LINK_LOSS_SERVICE_UUID = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb")
val ALERT_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A06-0000-1000-8000-00805f9b34fb")

private val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
private val BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

internal class PRXManager(
    context: Context,
    private val scope: CoroutineScope,
    private val logger: ToolboxLogger
) : BleManager(context) {

    private var batteryLevelCharacteristic: BluetoothGattCharacteristic? = null
    private var alertLevelCharacteristic: BluetoothGattCharacteristic? = null
    private var linkLossCharacteristic: BluetoothGattCharacteristic? = null

    private var localAlertLevelCharacteristic: BluetoothGattCharacteristic? = null
    private var linkLossServerCharacteristic: BluetoothGattCharacteristic? = null

    private var isAlertEnabled = false

    private val data = MutableStateFlow(PRXData())
    val dataHolder = ConnectionObserverAdapter<PRXData>()

    init {
        setConnectionObserver(dataHolder)

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

    private inner class ProximityManagerGattCallback : BleManagerGattCallback() {
        override fun initialize() {
            super.initialize()

            setWriteCallback(localAlertLevelCharacteristic)
                .with(object : AlertLevelDataCallback() {
                    override fun onAlertLevelChanged(device: BluetoothDevice, level: Int) {
                        data.value = data.value.copy(localAlarmLevel = AlarmLevel.create(level))
                    }
                })

            setWriteCallback(linkLossServerCharacteristic)
                .with(object : AlertLevelDataCallback() {
                    override fun onAlertLevelChanged(device: BluetoothDevice, level: Int) {
                        data.value = data.value.copy(linkLossAlarmLevel = AlarmLevel.create(level))
                    }
                })

            writeCharacteristic(
                linkLossCharacteristic,
                AlertLevelData.highAlert(),
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            ).enqueue()

            setNotificationCallback(batteryLevelCharacteristic)
                .asValidResponseFlow<BatteryLevelResponse>()
                .onEach {
                    data.value = data.value.copy(batteryLevel = it.batteryLevel)
                }.launchIn(scope)
            enableNotifications(batteryLevelCharacteristic).enqueue()
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

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            gatt.getService(LINK_LOSS_SERVICE_UUID)?.run {
                linkLossCharacteristic = getCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID)
            }
            gatt.getService(BATTERY_SERVICE_UUID)?.run {
                batteryLevelCharacteristic = getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)
            }
            gatt.getService(PRX_SERVICE_UUID)?.run {
                alertLevelCharacteristic = getCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID)
            }
            return linkLossCharacteristic != null
        }

        override fun onServicesInvalidated() {
            batteryLevelCharacteristic = null
            alertLevelCharacteristic = null
            linkLossCharacteristic = null
            localAlertLevelCharacteristic = null
            linkLossServerCharacteristic = null
            isAlertEnabled = false
        }
    }

    fun writeImmediateAlert(on: Boolean) {
        if (!isConnected) return
        scope.launchWithCatch {
            writeCharacteristic(
                alertLevelCharacteristic,
                if (on) AlertLevelData.highAlert() else AlertLevelData.noAlert(),
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            ).suspend()

            isAlertEnabled = on
            data.value = data.value.copy(isRemoteAlarm = on)
        }
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return ProximityManagerGattCallback()
    }
}
