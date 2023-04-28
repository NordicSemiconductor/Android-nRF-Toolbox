/*
 * Copyright (c) 2022, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.uart.repository

import android.annotation.SuppressLint
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.logger.NordicBlekLogger
import no.nordicsemi.android.kotlin.ble.client.main.callback.BleGattClient
import no.nordicsemi.android.kotlin.ble.client.main.connect
import no.nordicsemi.android.kotlin.ble.client.main.service.BleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.client.main.service.BleGattServices
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.kotlin.ble.core.data.Mtu
import no.nordicsemi.android.kotlin.ble.profile.battery.BatteryLevelParser
import no.nordicsemi.android.service.DEVICE_DATA
import no.nordicsemi.android.service.NotificationService
import no.nordicsemi.android.ui.view.StringConst
import java.util.*
import javax.inject.Inject

val UART_SERVICE_UUID: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
private val UART_RX_CHARACTERISTIC_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
private val UART_TX_CHARACTERISTIC_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")

private val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
private val BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

@SuppressLint("MissingPermission")
@AndroidEntryPoint
internal class UARTService : NotificationService() {

    @Inject
    lateinit var repository: UARTRepository

    @Inject
    lateinit var stringConst: StringConst

    private lateinit var client: BleGattClient

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        repository.setServiceRunning(true)

        val device = intent!!.getParcelableExtra<ServerDevice>(DEVICE_DATA)!!

        startGattClient(device)

        repository.stopEvent
            .onEach { disconnect() }
            .launchIn(lifecycleScope)

        return START_REDELIVER_INTENT
    }

    private fun startGattClient(device: ServerDevice) = lifecycleScope.launch {
        val logger = NordicBlekLogger(this@UARTService, stringConst.APP_NAME, "UART", device.address)

        client = device.connect(this@UARTService, logger = logger)

        client.requestMtu(Mtu.max)

        repository.loggerEvent
            .onEach { logger.launch() }
            .launchIn(lifecycleScope)

        client.connectionStateWithStatus
            .onEach { repository.onConnectionStateChanged(it) }
            .filterNotNull()
            .onEach { stopIfDisconnected(it) }
            .launchIn(lifecycleScope)

        if (!client.isConnected) {
            return@launch
        }

        client.discoverServices()
            .filterNotNull()
            .onEach { configureGatt(it, logger) }
            .launchIn(lifecycleScope)
    }

    private suspend fun configureGatt(services: BleGattServices, logger: NordicBlekLogger) {
        val uartService = services.findService(UART_SERVICE_UUID)!!
        val rxCharacteristic = uartService.findCharacteristic(UART_RX_CHARACTERISTIC_UUID)!!
        val txCharacteristic = uartService.findCharacteristic(UART_TX_CHARACTERISTIC_UUID)!!

        val batteryService = services.findService(BATTERY_SERVICE_UUID)

        batteryService?.findCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)?.getNotifications()
            ?.mapNotNull { BatteryLevelParser.parse(it) }
            ?.onEach { repository.onBatteryLevelChanged(it) }
            ?.launchIn(lifecycleScope)

        txCharacteristic.getNotifications()
            .onEach { repository.onNewMessageReceived(String(it)) }
            .onEach { logger.log(10, "Received: ${String(it)}") }
            .launchIn(lifecycleScope)

        repository.command
            .onEach { rxCharacteristic.splitWrite(it.toByteArray(), getWriteType(rxCharacteristic)) }
            .onEach { repository.onNewMessageSent(it) }
            .onEach { logger.log(10, "Sent: $it") }
            .launchIn(lifecycleScope)
    }

    private fun getWriteType(characteristic: BleGattCharacteristic): BleWriteType {
        return if (characteristic.properties.contains(BleGattProperty.PROPERTY_WRITE)) {
            BleWriteType.DEFAULT
        } else {
            BleWriteType.NO_RESPONSE
        }
    }

    private fun stopIfDisconnected(connectionState: GattConnectionStateWithStatus) {
        if (connectionState.state == GattConnectionState.STATE_DISCONNECTED) {
            stopSelf()
        }
    }

    private fun disconnect() {
        client.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        repository.setServiceRunning(false)
    }
}
