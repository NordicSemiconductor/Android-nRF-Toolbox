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
import no.nordicsemi.android.common.logger.NordicBlekLogger
import no.nordicsemi.android.kotlin.ble.client.main.callback.BleGattClient
import no.nordicsemi.android.kotlin.ble.client.main.connect
import no.nordicsemi.android.kotlin.ble.client.main.service.BleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.client.main.service.BleGattServices
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.profile.battery.BatteryLevelParser
import no.nordicsemi.android.kotlin.ble.profile.prx.AlarmLevel
import no.nordicsemi.android.kotlin.ble.profile.prx.AlarmLevelParser
import no.nordicsemi.android.kotlin.ble.profile.prx.AlertLevelInputParser
import no.nordicsemi.android.kotlin.ble.server.main.BleGattServer
import no.nordicsemi.android.kotlin.ble.server.main.service.BleGattServerServiceType
import no.nordicsemi.android.kotlin.ble.server.main.service.BleServerGattCharacteristicConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.BleServerGattServiceConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.BluetoothGattServerConnection
import no.nordicsemi.android.service.DEVICE_DATA
import no.nordicsemi.android.service.NotificationService
import no.nordicsemi.android.ui.view.StringConst
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

    @Inject
    lateinit var stringConst: StringConst

    private lateinit var client: BleGattClient
    private lateinit var server: BleGattServer

    private lateinit var alertLevelCharacteristic: BleGattCharacteristic

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val device = intent!!.getParcelableExtra<ServerDevice>(DEVICE_DATA)!!

        startServer(device)

        repository.stopEvent
            .onEach { disconnect() }
            .launchIn(lifecycleScope)

        return START_REDELIVER_INTENT
    }

    private fun startServer(device: ServerDevice) = lifecycleScope.launch {
        val alertLevelCharacteristic = BleServerGattCharacteristicConfig(
            uuid = ALERT_LEVEL_CHARACTERISTIC_UUID,
            properties = listOf(BleGattProperty.PROPERTY_WRITE_NO_RESPONSE),
            permissions = listOf(BleGattPermission.PERMISSION_WRITE)
        )
        val prxServiceConfig = BleServerGattServiceConfig(
            uuid = PRX_SERVICE_UUID,
            type = BleGattServerServiceType.SERVICE_TYPE_PRIMARY,
            characteristicConfigs = listOf(alertLevelCharacteristic)
        )

        val linkLossCharacteristic = BleServerGattCharacteristicConfig(
            uuid = ALERT_LEVEL_CHARACTERISTIC_UUID,
            properties = listOf(BleGattProperty.PROPERTY_WRITE, BleGattProperty.PROPERTY_READ),
            permissions = listOf(BleGattPermission.PERMISSION_WRITE, BleGattPermission.PERMISSION_READ),
            initialValue = AlertLevelInputParser.parse(AlarmLevel.HIGH)
        )

        val linkLossServiceConfig = BleServerGattServiceConfig(
            uuid = LINK_LOSS_SERVICE_UUID,
            type = BleGattServerServiceType.SERVICE_TYPE_PRIMARY,
            characteristicConfigs = listOf(linkLossCharacteristic)
        )

        server = BleGattServer.create(this@PRXService, prxServiceConfig, linkLossServiceConfig)

        //Order is important. We don't want to connect before services have been added to the server.
        startGattClient(device)

        server.onNewConnection
            .onEach { setUpServerConnection(it.second) }
            .launchIn(lifecycleScope)
    }

    private fun setUpServerConnection(connection: BluetoothGattServerConnection) {
        val prxService = connection.services.findService(PRX_SERVICE_UUID)!!
        val linkLossService = connection.services.findService(LINK_LOSS_SERVICE_UUID)!!

        val prxCharacteristic = prxService.findCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID)!!
        val linkLossCharacteristic = linkLossService.findCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID)!!

        prxCharacteristic.value
            .mapNotNull { AlarmLevelParser.parse(it) }
            .onEach { repository.setLocalAlarmLevel(it) }
            .launchIn(lifecycleScope)

        linkLossCharacteristic.value
            .mapNotNull { AlarmLevelParser.parse(it) }
            .onEach { repository.setLinkLossAlarmLevel(it) }
            .launchIn(lifecycleScope)
    }

    private fun startGattClient(device: ServerDevice) = lifecycleScope.launch {
        val logger = NordicBlekLogger(this@PRXService, stringConst.APP_NAME, "PRX", device.address)

        client = device.connect(this@PRXService, logger = logger)

        repository.loggerEvent
            .onEach { logger.launch() }
            .launchIn(lifecycleScope)

        client.connectionStateWithStatus
            .filterNotNull()
            .onEach { repository.onConnectionStateChanged(it) }
            .filterNotNull()
            .onEach { stopIfDisconnected(it.state, it.status) }
            .launchIn(lifecycleScope)

        if (!client.isConnected) {
            repository.onInitComplete(device)
            return@launch
        }

        client.discoverServices()
            .filterNotNull()
            .onEach { configureGatt(it, device) }
            .launchIn(lifecycleScope)

        repository.remoteAlarmLevel
            .onEach { writeAlertLevel(it) }
            .launchIn(lifecycleScope)
    }

    private suspend fun configureGatt(services: BleGattServices, device: ServerDevice) {
        val prxService = services.findService(PRX_SERVICE_UUID)!!
        alertLevelCharacteristic = prxService.findCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID)!!
        val linkLossService = services.findService(LINK_LOSS_SERVICE_UUID)!!
        val linkLossCharacteristic = linkLossService.findCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID)!!
        val batteryService = services.findService(BATTERY_SERVICE_UUID)!!
        val batteryLevelCharacteristic = batteryService.findCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)!!

        batteryLevelCharacteristic.getNotifications()
            .mapNotNull { BatteryLevelParser.parse(it) }
            .onEach { repository.onBatteryLevelChanged(it) }
            .launchIn(lifecycleScope)

        linkLossCharacteristic.write(AlertLevelInputParser.parse(AlarmLevel.HIGH))

        repository.onInitComplete(device)
    }

    private suspend fun writeAlertLevel(alarmLevel: AlarmLevel) {
        alertLevelCharacteristic.write(AlertLevelInputParser.parse(alarmLevel), BleWriteType.NO_RESPONSE)
        repository.onRemoteAlarmLevelSet(alarmLevel)
    }

    private fun stopIfDisconnected(connectionState: GattConnectionState, connectionStatus: BleGattConnectionStatus) {
        if (connectionState == GattConnectionState.STATE_DISCONNECTED && !connectionStatus.isLinkLoss) {
            server.stopServer()
            repository.stop()
            stopSelf()
        }
    }

    private fun disconnect() {
        client.disconnect()
        server.stopServer()
    }
}
