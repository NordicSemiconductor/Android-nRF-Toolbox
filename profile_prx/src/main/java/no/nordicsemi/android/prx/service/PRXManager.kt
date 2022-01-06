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
package no.nordicsemi.android.prx.service

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.content.Context
import android.util.Log
import no.nordicsemi.android.ble.callback.FailCallback
import no.nordicsemi.android.ble.common.callback.alert.AlertLevelDataCallback
import no.nordicsemi.android.ble.common.data.alert.AlertLevelData
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.error.GattError
import no.nordicsemi.android.log.LogContract
import no.nordicsemi.android.prx.data.PRXRepository
import no.nordicsemi.android.service.BatteryManager
import java.util.*

/** Link Loss service UUID.  */
val LINK_LOSS_SERVICE_UUID = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb")

/** Immediate Alert service UUID.  */
val PRX_SERVICE_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb")

/** Alert Level characteristic UUID.  */
val ALERT_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A06-0000-1000-8000-00805f9b34fb")

internal class PRXManager(
    context: Context,
    private val dataHolder: PRXRepository
) : BatteryManager(context) {

    // Client characteristics.
    private var alertLevelCharacteristic: BluetoothGattCharacteristic? = null
    private var linkLossCharacteristic: BluetoothGattCharacteristic? = null

    // Server characteristics.
    private var localAlertLevelCharacteristic: BluetoothGattCharacteristic? = null
    /**
     * Returns true if the alert has been enabled on the proximity tag, false otherwise.
     */
    /** A flag indicating whether the alarm on the connected proximity tag has been activated.  */
    var isAlertEnabled = false
        private set

    /**
     * BluetoothGatt callbacks for connection/disconnection, service discovery,
     * receiving indication, etc.
     */
    private inner class ProximityManagerGattCallback : BatteryManagerGattCallback() {
        override fun initialize() {
            super.initialize()
            // This callback will be called whenever local Alert Level char is written
            // by a connected proximity tag.
            setWriteCallback(localAlertLevelCharacteristic)
                .with(object : AlertLevelDataCallback() {
                    override fun onAlertLevelChanged(device: BluetoothDevice, level: Int) {
                        dataHolder.setLocalAlarmLevel(level)
                    }
                })
            // After connection, set the Link Loss behaviour on the tag.
            writeCharacteristic(linkLossCharacteristic, AlertLevelData.highAlert())
                .done { device: BluetoothDevice? ->
                    log(
                        Log.INFO,
                        "Link loss alert level set"
                    )
                }
                .fail { device: BluetoothDevice?, status: Int ->
                    log(
                        Log.WARN,
                        "Failed to set link loss level: $status"
                    )
                }
                .enqueue()
        }

        override fun onServerReady(server: BluetoothGattServer) {
            val immediateAlertService = server.getService(PRX_SERVICE_UUID)
            if (immediateAlertService != null) {
                localAlertLevelCharacteristic = immediateAlertService.getCharacteristic(
                    ALERT_LEVEL_CHARACTERISTIC_UUID
                )
            }
        }

        override fun onServicesInvalidated() { }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val llService = gatt.getService(LINK_LOSS_SERVICE_UUID)
            if (llService != null) {
                linkLossCharacteristic =
                    llService.getCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID)
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
            // Reset the alert flag
            isAlertEnabled = false
        }
    }

    /**
     * Toggles the immediate alert on the target device.
     */
    fun toggleImmediateAlert() {
        writeImmediateAlert(!isAlertEnabled)
    }

    /**
     * Writes the HIGH ALERT or NO ALERT command to the target device.
     *
     * @param on true to enable the alarm on proximity tag, false to disable it.
     */
    fun writeImmediateAlert(on: Boolean) {
        if (!isConnected()) return
        writeCharacteristic(
            alertLevelCharacteristic,
            if (on) AlertLevelData.highAlert() else AlertLevelData.noAlert()
        )
            .before { device: BluetoothDevice? ->
                log(
                    Log.VERBOSE,
                    if (on) "Setting alarm to HIGH..." else "Disabling alarm..."
                )
            }
            .with { _: BluetoothDevice, data: Data ->
                log(
                    LogContract.Log.Level.APPLICATION,
                    "\"" + PRXAlertLevelParser.parse(data) + "\" sent"
                )
            }
            .done { device: BluetoothDevice? ->
                isAlertEnabled = on
                dataHolder.setRemoteAlarmLevel(on)
            }
            .fail { device: BluetoothDevice?, status: Int ->
                log(
                    Log.WARN,
                    if (status == FailCallback.REASON_NULL_ATTRIBUTE) "Alert Level characteristic not found" else GattError.parse(
                        status
                    )
                )
            }
            .enqueue()
    }

    override fun onBatteryLevelChanged(batteryLevel: Int) {
        dataHolder.setBatteryLevel(batteryLevel)
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return ProximityManagerGattCallback()
    }
}
