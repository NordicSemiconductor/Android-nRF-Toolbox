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
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.core.DataByteArray
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.Mtu
import no.nordicsemi.android.kotlin.ble.profile.battery.BatteryLevelParser
import no.nordicsemi.android.service.DEVICE_DATA
import no.nordicsemi.android.service.NotificationService
import java.util.*
import javax.inject.Inject

val UART_SERVICE_UUID: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
internal val UART_RX_CHARACTERISTIC_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
internal val UART_TX_CHARACTERISTIC_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")

internal val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
internal val BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

@SuppressLint("MissingPermission")
@AndroidEntryPoint
internal class UARTService : NotificationService() {

    @Inject
    lateinit var repository: UARTRepository

    private var client: ClientBleGatt? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        repository.setServiceRunning(true)

        val device = IntentCompat.getParcelableExtra(intent!!, DEVICE_DATA, ServerDevice::class.java)!!

        startGattClient(device)

        repository.stopEvent
            .onEach { disconnect() }
            .launchIn(lifecycleScope)

        return START_REDELIVER_INTENT
    }

    private fun startGattClient(device: ServerDevice) = lifecycleScope.launch {
        val client = ClientBleGatt.connect(this@UARTService, device, lifecycleScope, logger = { p, s -> repository.log(p, s) })
        this@UARTService.client = client

        if (!client.isConnected) {
            return@launch
        }

        try {
            client.requestMtu(Mtu.max)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val services = client.discoverServices()
            configureGatt(services)
        } catch (e: Exception) {
            repository.onMissingServices()
        }

        client.connectionStateWithStatus
            .filterNotNull()
            .onEach { repository.onConnectionStateChanged(it) }
            .onEach { stopIfDisconnected(it.state, it.status) }
            .filterNotNull()
            .launchIn(lifecycleScope)
    }

    private suspend fun configureGatt(services: ClientBleGattServices) {
        val uartService = services.findService(UART_SERVICE_UUID)!!
        val rxCharacteristic = uartService.findCharacteristic(UART_RX_CHARACTERISTIC_UUID)!!
        val txCharacteristic = uartService.findCharacteristic(UART_TX_CHARACTERISTIC_UUID)!!

        txCharacteristic.getNotifications()
            .map { String(it.value) }
            .onEach { repository.onNewMessageReceived(it) }
            .onEach { repository.log(10, "Received: $it") }
            .catch { it.printStackTrace() }
            .launchIn(lifecycleScope)

        repository.command
            .onEach { rxCharacteristic.splitWrite(DataByteArray.from(it), getWriteType(rxCharacteristic)) }
            .onEach { repository.onNewMessageSent(it) }
            .onEach { repository.log(10, "Sent: $it") }
            .catch { it.printStackTrace() }
            .launchIn(lifecycleScope)

        // Battery service is optional
        services.findService(BATTERY_SERVICE_UUID)
            ?.findCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)
            ?.getNotifications()
            ?.mapNotNull { BatteryLevelParser.parse(it) }
            ?.onEach { repository.onBatteryLevelChanged(it) }
            ?.catch { it.printStackTrace() }
            ?.launchIn(lifecycleScope)
    }

    private fun getWriteType(characteristic: ClientBleGattCharacteristic): BleWriteType {
        return if (characteristic.properties.contains(BleGattProperty.PROPERTY_WRITE)) {
            BleWriteType.DEFAULT
        } else {
            BleWriteType.NO_RESPONSE
        }
    }

    private fun stopIfDisconnected(connectionState: GattConnectionState, connectionStatus: BleGattConnectionStatus) {
        if (connectionState == GattConnectionState.STATE_DISCONNECTED && !connectionStatus.isLinkLoss) {
            repository.disconnect()
            stopSelf()
        }
    }

    private fun disconnect() {
        client?.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        repository.setServiceRunning(false)
    }
}
