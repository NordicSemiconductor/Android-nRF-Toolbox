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
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectOptions
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.profile.battery.BatteryLevelParser
import no.nordicsemi.android.kotlin.ble.profile.prx.AlarmLevel
import no.nordicsemi.android.kotlin.ble.profile.prx.AlarmLevelParser
import no.nordicsemi.android.kotlin.ble.profile.prx.AlertLevelInputParser
import no.nordicsemi.android.kotlin.ble.server.main.ServerBleGatt
import no.nordicsemi.android.kotlin.ble.server.main.ServerConnectionEvent
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattCharacteristicConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServiceConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServiceType
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBluetoothGattConnection
import no.nordicsemi.android.service.DEVICE_DATA
import no.nordicsemi.android.service.NotificationService
import no.nordicsemi.android.utils.tryOrLog
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

    private var client: ClientBleGatt? = null
    private var server: ServerBleGatt? = null

    private var alertLevelCharacteristic: ClientBleGattCharacteristic? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        repository.setServiceRunning(true)

        val device = IntentCompat.getParcelableExtra(intent!!, DEVICE_DATA, ServerDevice::class.java)!!

        startServer(device)

        repository.stopEvent
            .onEach { disconnect() }
            .launchIn(lifecycleScope)

        return START_REDELIVER_INTENT
    }

    private fun startServer(device: ServerDevice) = lifecycleScope.launch {
        val alertLevelCharacteristic = ServerBleGattCharacteristicConfig(
            uuid = ALERT_LEVEL_CHARACTERISTIC_UUID,
            properties = listOf(BleGattProperty.PROPERTY_WRITE_NO_RESPONSE),
            permissions = listOf(BleGattPermission.PERMISSION_WRITE)
        )
        val prxServiceConfig = ServerBleGattServiceConfig(
            uuid = PRX_SERVICE_UUID,
            type = ServerBleGattServiceType.SERVICE_TYPE_PRIMARY,
            characteristicConfigs = listOf(alertLevelCharacteristic)
        )

        val linkLossCharacteristic = ServerBleGattCharacteristicConfig(
            uuid = ALERT_LEVEL_CHARACTERISTIC_UUID,
            properties = listOf(BleGattProperty.PROPERTY_WRITE, BleGattProperty.PROPERTY_READ),
            permissions = listOf(BleGattPermission.PERMISSION_WRITE, BleGattPermission.PERMISSION_READ),
            initialValue = AlertLevelInputParser.parse(AlarmLevel.HIGH)
        )

        val linkLossServiceConfig = ServerBleGattServiceConfig(
            uuid = LINK_LOSS_SERVICE_UUID,
            type =ServerBleGattServiceType.SERVICE_TYPE_PRIMARY,
            characteristicConfigs = listOf(linkLossCharacteristic)
        )

        val server = ServerBleGatt.create(this@PRXService, lifecycleScope, prxServiceConfig, linkLossServiceConfig)
        this@PRXService.server = server

        //Order is important. We don't want to connect before services have been added to the server.
        startGattClient(device)

        server.connectionEvents
            .mapNotNull { (it as? ServerConnectionEvent.DeviceConnected)?.connection }
            .onEach { setUpServerConnection(it) }
            .launchIn(lifecycleScope)
    }

    private fun setUpServerConnection(connection: ServerBluetoothGattConnection) {
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
        val client = ClientBleGatt.connect(
            this@PRXService,
            device,
            lifecycleScope,
            logger = { p, s -> repository.log(p, s) },
            options = BleGattConnectOptions(autoConnect = true)
        )
        this@PRXService.client = client

        client.waitForBonding()

        client.connectionStateWithStatus
            .filterNotNull()
            .onEach { repository.onConnectionStateChanged(it) }
            .onEach { stopIfDisconnected(it.state, it.status) }
            .launchIn(lifecycleScope)

        if (!client.isConnected) {
            return@launch
        }

        try {
            val services = client.discoverServices()
            configureGatt(services)
        } catch (e: Exception) {
            repository.onMissingServices()
        }

        repository.remoteAlarmLevel
            .onEach { writeAlertLevel(it) }
            .launchIn(lifecycleScope)
    }

    private suspend fun configureGatt(services: ClientBleGattServices) {
        val prxService = services.findService(PRX_SERVICE_UUID)!!
        alertLevelCharacteristic = prxService.findCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID)!!
        val linkLossService = services.findService(LINK_LOSS_SERVICE_UUID)!!
        val linkLossCharacteristic = linkLossService.findCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID)!!

        // Battery service is optional
        services.findService(BATTERY_SERVICE_UUID)
            ?.findCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)
            ?.getNotifications()
            ?.mapNotNull { BatteryLevelParser.parse(it) }
            ?.onEach { repository.onBatteryLevelChanged(it) }
            ?.catch { it.printStackTrace() }
            ?.launchIn(lifecycleScope)

        tryOrLog {
            linkLossCharacteristic.write(AlertLevelInputParser.parse(AlarmLevel.HIGH))
        }
    }

    private suspend fun writeAlertLevel(alarmLevel: AlarmLevel) {
        try {
            alertLevelCharacteristic?.run {
                write(AlertLevelInputParser.parse(alarmLevel), BleWriteType.NO_RESPONSE)
                repository.onRemoteAlarmLevelSet(alarmLevel)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopIfDisconnected(connectionState: GattConnectionState, connectionStatus: BleGattConnectionStatus) {
        if (connectionState == GattConnectionState.STATE_DISCONNECTED && !connectionStatus.isLinkLoss) {
            server?.stopServer()
            repository.disconnect()
            stopSelf()
        }
    }

    private fun disconnect() {
        client?.disconnect()
        server?.stopServer()
    }

    override fun onDestroy() {
        super.onDestroy()
        repository.setServiceRunning(false)
    }
}
