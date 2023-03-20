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

package no.nordicsemi.android.prx.repository

import android.annotation.SuppressLint
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.client.callback.BleGattClient
import no.nordicsemi.android.kotlin.ble.core.client.service.BleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.client.service.BleGattServices
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.server.BleGattServer
import no.nordicsemi.android.kotlin.ble.core.server.service.service.BleGattServerServiceType
import no.nordicsemi.android.kotlin.ble.core.server.service.service.BleServerGattCharacteristicConfig
import no.nordicsemi.android.kotlin.ble.core.server.service.service.BleServerGattServiceConfig
import no.nordicsemi.android.kotlin.ble.profile.battery.BatteryLevelParser
import no.nordicsemi.android.kotlin.ble.profile.hts.HTSDataParser
import no.nordicsemi.android.service.DEVICE_DATA
import no.nordicsemi.android.service.NotificationService
import java.util.*
import javax.inject.Inject

val PRX_SERVICE_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb")
private val LINK_LOSS_SERVICE_UUID = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb")
private val ALERT_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A06-0000-1000-8000-00805f9b34fb")

private val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
private val BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

@SuppressLint("MissingPermission")
@AndroidEntryPoint
internal class PRXService : NotificationService() {

    @Inject
    lateinit var repository: PRXRepository

    private lateinit var client: BleGattClient

    private lateinit var alertLevelCharacteristic: BleGattCharacteristic

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val device = intent!!.getParcelableExtra<ServerDevice>(DEVICE_DATA)!!

        startGattClient(device)

        repository.stopEvent
            .onEach { disconnect() }
            .launchIn(lifecycleScope)

        return START_REDELIVER_INTENT
    }

    private fun startServer() {
        val alertLevelCharacteristic = BleServerGattCharacteristicConfig(
            ALERT_LEVEL_CHARACTERISTIC_UUID,
            listOf(BleGattProperty.PROPERTY_WRITE_NO_RESPONSE),
            listOf(BleGattPermission.PERMISSION_WRITE)
        )

        val linkLossCharacteristic = BleServerGattCharacteristicConfig(
            LINK_LOSS_SERVICE_UUID,
            listOf(BleGattProperty.PROPERTY_WRITE, BleGattProperty.PROPERTY_READ),
            listOf(BleGattPermission.PERMISSION_WRITE, BleGattPermission.PERMISSION_READ)
        )

        val serviceConfig = BleServerGattServiceConfig(
            PRX_SERVICE_UUID,
            BleGattServerServiceType.SERVICE_TYPE_PRIMARY,
            listOf(alertLevelCharacteristic, linkLossCharacteristic)
        )

        val server = BleGattServer.create(this@PRXService, serviceConfig)

        TODO("Initialize characteristic with value")
    }

    private fun startGattClient(device: ServerDevice) = lifecycleScope.launch {
        client = device.connect(this@PRXService)

        client.connectionState
            .onEach { repository.onConnectionStateChanged(it) }
            .filterNotNull()
            .onEach { stopIfDisconnected(it) }
            .launchIn(lifecycleScope)

        client.services
            .filterNotNull()
            .onEach { configureGatt(it, device) }
            .launchIn(lifecycleScope)
    }

    private suspend fun configureGatt(services: BleGattServices, device: ServerDevice) {
        val prxService = services.findService(PRX_SERVICE_UUID)!!
        alertLevelCharacteristic = prxService.findCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID)!!
        val linkLossCharacteristic = prxService.findCharacteristic(LINK_LOSS_SERVICE_UUID)!!
        val batteryService = services.findService(BATTERY_SERVICE_UUID)!!
        val batteryLevelCharacteristic = batteryService.findCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)!!

        batteryLevelCharacteristic.getNotifications()
            .mapNotNull { BatteryLevelParser.parse(it) }
            .onEach { repository.onBatteryLevelChanged(it) }
            .launchIn(lifecycleScope)

        linkLossCharacteristic.write(Alert)

        htsMeasurementCharacteristic.getNotifications()
            .mapNotNull { HTSDataParser.parse(it) }
            .onEach { repository.onHTSDataChanged(it) }
            .launchIn(lifecycleScope)

        repository.onInitComplete(device)
    }

    private fun stopIfDisconnected(connectionState: GattConnectionState) {
        if (connectionState == GattConnectionState.STATE_DISCONNECTED) {
            stopSelf()
        }
    }

    private fun disconnect() {
        client.disconnect()
    }
}
