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

package no.nordicsemi.android.hts.repository

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
import no.nordicsemi.android.kotlin.ble.client.main.service.BleGattServices
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.kotlin.ble.profile.battery.BatteryLevelParser
import no.nordicsemi.android.kotlin.ble.profile.hts.HTSDataParser
import no.nordicsemi.android.service.DEVICE_DATA
import no.nordicsemi.android.service.NotificationService
import no.nordicsemi.android.ui.view.StringConst
import java.util.*
import javax.inject.Inject

val HTS_SERVICE_UUID: UUID = UUID.fromString("00001809-0000-1000-8000-00805f9b34fb")
private val HTS_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A1C-0000-1000-8000-00805f9b34fb")

private val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
private val BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

@SuppressLint("MissingPermission")
@AndroidEntryPoint
internal class HTSService : NotificationService() {

    @Inject
    lateinit var repository: HTSRepository

    @Inject
    lateinit var stringConst: StringConst

    private lateinit var client: BleGattClient

    private var hasBeenInitialized: Boolean = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val device = intent!!.getParcelableExtra<ServerDevice>(DEVICE_DATA)!!

        startGattClient(device)

        repository.stopEvent
            .onEach { disconnect() }
            .launchIn(lifecycleScope)

        return START_REDELIVER_INTENT
    }

    private fun startGattClient(device: ServerDevice) = lifecycleScope.launch {
        val logger = NordicBlekLogger(this@HTSService, stringConst.APP_NAME, "HTS", device.address)

        client = device.connect(this@HTSService, logger = logger)

        repository.loggerEvent
            .onEach { logger.launch() }
            .launchIn(lifecycleScope)

        client.connectionStateWithStatus
            .onEach { repository.onConnectionStateChanged(it) }
            .filterNotNull()
            .onEach { stopIfDisconnected(it) }
            .onEach { unlockUiIfDisconnected(it, device) }
            .launchIn(lifecycleScope)

        if (!client.isConnected) {
            hasBeenInitialized = true
            repository.onInitComplete(device)
            return@launch
        }

        client.discoverServices()
            .filterNotNull()
            .onEach { configureGatt(it, device) }
            .launchIn(lifecycleScope)
    }

    private suspend fun configureGatt(services: BleGattServices, device: ServerDevice) {
        val htsService = services.findService(HTS_SERVICE_UUID)!!
        val htsMeasurementCharacteristic = htsService.findCharacteristic(HTS_MEASUREMENT_CHARACTERISTIC_UUID)!!
        val batteryService = services.findService(BATTERY_SERVICE_UUID)!!
        val batteryLevelCharacteristic = batteryService.findCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)!!

        batteryLevelCharacteristic.getNotifications()
            .mapNotNull { BatteryLevelParser.parse(it) }
            .onEach { repository.onBatteryLevelChanged(it) }
            .launchIn(lifecycleScope)

        htsMeasurementCharacteristic.getNotifications()
            .mapNotNull { HTSDataParser.parse(it) }
            .onEach { repository.onHTSDataChanged(it) }
            .launchIn(lifecycleScope)

        hasBeenInitialized = true
        repository.onInitComplete(device)
    }

    private fun stopIfDisconnected(connectionState: GattConnectionStateWithStatus) {
        if (connectionState.state == GattConnectionState.STATE_DISCONNECTED) {
            stopSelf()
        }
    }

    private fun unlockUiIfDisconnected(connectionState: GattConnectionStateWithStatus, device: ServerDevice) {
        if (connectionState.state == GattConnectionState.STATE_DISCONNECTED && !hasBeenInitialized) {
            repository.onInitComplete(device)
        }
    }

    private fun disconnect() {
        client.disconnect()
    }
}
