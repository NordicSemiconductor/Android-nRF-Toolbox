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
package no.nordicsemi.android.uart.data

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.text.TextUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.WriteRequest
import no.nordicsemi.android.ble.common.callback.battery.BatteryLevelResponse
import no.nordicsemi.android.ble.ktx.asFlow
import no.nordicsemi.android.ble.ktx.asValidResponseFlow
import no.nordicsemi.android.service.ConnectionObserverAdapter
import no.nordicsemi.android.utils.EMPTY
import no.nordicsemi.android.utils.launchWithCatch
import java.util.*

val UART_SERVICE_UUID: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
private val UART_RX_CHARACTERISTIC_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
private val UART_TX_CHARACTERISTIC_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")

private val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
private val BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

internal class UARTManager(
    context: Context,
    private val scope: CoroutineScope,
) : BleManager(context) {

    private var batteryLevelCharacteristic: BluetoothGattCharacteristic? = null

    private var rxCharacteristic: BluetoothGattCharacteristic? = null
    private var txCharacteristic: BluetoothGattCharacteristic? = null

    private var useLongWrite = true

    private val data = MutableStateFlow(UARTData())
    val dataHolder = ConnectionObserverAdapter<UARTData>()

    init {
        setConnectionObserver(dataHolder)

        data.onEach {
            dataHolder.setValue(it)
        }.launchIn(scope)
    }

    private inner class UARTManagerGattCallback : BleManagerGattCallback() {

        override fun initialize() {
            setNotificationCallback(txCharacteristic).asFlow().onEach {
                val text: String = it.getStringValue(0) ?: String.EMPTY
                data.value = data.value.copy(text = text)
            }

            requestMtu(260).enqueue()
            enableNotifications(txCharacteristic).enqueue()

            setNotificationCallback(batteryLevelCharacteristic).asValidResponseFlow<BatteryLevelResponse>().onEach {
                data.value = data.value.copy(batteryLevel = it.batteryLevel)
            }.launchIn(scope)
            enableNotifications(batteryLevelCharacteristic).enqueue()
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service: BluetoothGattService = gatt.getService(UART_SERVICE_UUID)
            if (service != null) {
                rxCharacteristic = service.getCharacteristic(UART_RX_CHARACTERISTIC_UUID)
                txCharacteristic = service.getCharacteristic(UART_TX_CHARACTERISTIC_UUID)
            }
            var writeRequest = false
            var writeCommand = false

            rxCharacteristic?.let {
                val rxProperties: Int = it.properties
                writeRequest = rxProperties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0
                writeCommand =
                    rxProperties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0

                // Set the WRITE REQUEST type when the characteristic supports it.
                // This will allow to send long write (also if the characteristic support it).
                // In case there is no WRITE REQUEST property, this manager will divide texts
                // longer then MTU-3 bytes into up to MTU-3 bytes chunks.
                if (writeRequest) {
                    it.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                } else {
                    useLongWrite = false
                }
            }
            gatt.getService(BATTERY_SERVICE_UUID)?.run {
                batteryLevelCharacteristic = getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)
            }
            return rxCharacteristic != null && txCharacteristic != null && (writeRequest || writeCommand)
        }

        override fun onServicesInvalidated() {
            batteryLevelCharacteristic = null
            rxCharacteristic = null
            txCharacteristic = null
            useLongWrite = true
        }
    }

    fun send(text: String) {
        if (rxCharacteristic == null) return
        if (!TextUtils.isEmpty(text)) {
            scope.launchWithCatch {
                val request: WriteRequest = writeCharacteristic(rxCharacteristic, text.toByteArray(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                if (!useLongWrite) {
                    request.split()
                }
                request.enqueue()
            }
        }
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return UARTManagerGattCallback()
    }
}
