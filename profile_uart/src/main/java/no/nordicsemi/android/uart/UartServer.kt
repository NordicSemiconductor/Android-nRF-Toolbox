package no.nordicsemi.android.uart

import android.annotation.SuppressLint
import android.content.Context
import android.os.ParcelUuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.core.DataByteArray
import no.nordicsemi.android.kotlin.ble.advertiser.BleAdvertiser
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertisingConfig
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertisingData
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.server.main.ServerBleGatt
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattCharacteristicConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServiceConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServiceType
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBluetoothGattConnection
import no.nordicsemi.android.uart.repository.BATTERY_LEVEL_CHARACTERISTIC_UUID
import no.nordicsemi.android.uart.repository.BATTERY_SERVICE_UUID
import no.nordicsemi.android.uart.repository.UART_RX_CHARACTERISTIC_UUID
import no.nordicsemi.android.uart.repository.UART_SERVICE_UUID
import no.nordicsemi.android.uart.repository.UART_TX_CHARACTERISTIC_UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val STANDARD_DELAY = 1000L

@SuppressLint("MissingPermission")
@Singleton
class UartServer @Inject constructor(
    private val scope: CoroutineScope,
) {

    private lateinit var server: ServerBleGatt

    private lateinit var rxCharacteristic: ServerBleGattCharacteristic
    private lateinit var txCharacteristic: ServerBleGattCharacteristic
    private lateinit var batteryLevelCharacteristic: ServerBleGattCharacteristic

    fun start(
        context: Context,
        device: MockServerDevice = MockServerDevice(
            name = "Mock UART Server",
            address = "66:55:44:33:22:11"
        ),
    ) = scope.launch {
        val rxCharacteristic = ServerBleGattCharacteristicConfig(
            UART_RX_CHARACTERISTIC_UUID,
            listOf(BleGattProperty.PROPERTY_NOTIFY, BleGattProperty.PROPERTY_WRITE),
            listOf()
        )

        val txCharacteristic = ServerBleGattCharacteristicConfig(
            UART_TX_CHARACTERISTIC_UUID,
            listOf(BleGattProperty.PROPERTY_INDICATE, BleGattProperty.PROPERTY_WRITE),
            listOf(BleGattPermission.PERMISSION_WRITE)
        )

        val uartService = ServerBleGattServiceConfig(
            UART_SERVICE_UUID,
            ServerBleGattServiceType.SERVICE_TYPE_PRIMARY,
            listOf(rxCharacteristic, txCharacteristic)
        )

        val batteryLevelCharacteristic = ServerBleGattCharacteristicConfig(
            BATTERY_LEVEL_CHARACTERISTIC_UUID,
            listOf(BleGattProperty.PROPERTY_READ, BleGattProperty.PROPERTY_NOTIFY),
            listOf(BleGattPermission.PERMISSION_READ)
        )

        val batteryService = ServerBleGattServiceConfig(
            BATTERY_SERVICE_UUID,
            ServerBleGattServiceType.SERVICE_TYPE_PRIMARY,
            listOf(batteryLevelCharacteristic)
        )

        server = ServerBleGatt.create(
            context = context,
            scope = scope,
            config = arrayOf(uartService, batteryService),
            mock = device
        )

        val advertiser = BleAdvertiser.create(context)
        advertiser.advertise(
            config = BleAdvertisingConfig(
                advertiseData = BleAdvertisingData(
                    serviceUuid = ParcelUuid(UART_SERVICE_UUID)
                )
            ), mock = device
        ).launchIn(scope)

        launch {
            server.connections
                .mapNotNull { it.values.firstOrNull() }
                .collect { setUpConnection(it) }
        }
    }

    internal fun stopServer() {
        server.stopServer()
    }

    private fun setUpConnection(connection: ServerBluetoothGattConnection) {
        val glsService = connection.services.findService(UART_SERVICE_UUID)!!
        rxCharacteristic = glsService.findCharacteristic(UART_RX_CHARACTERISTIC_UUID)!!
        txCharacteristic = glsService.findCharacteristic(UART_TX_CHARACTERISTIC_UUID)!!

        rxCharacteristic.value.onEach {
            send(txCharacteristic, it)
        }.launchIn(scope)

        val batteryService = connection.services.findService(BATTERY_SERVICE_UUID)!!
        batteryLevelCharacteristic = batteryService
            .findCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)!!

        startBatteryService()
    }

    private fun startBatteryService() {
        scope.launch {
            repeat(100) {
                send(batteryLevelCharacteristic, DataByteArray.from(0x61))
                delay(STANDARD_DELAY)
                send(batteryLevelCharacteristic, DataByteArray.from(0x60))
                delay(STANDARD_DELAY)
                send(batteryLevelCharacteristic, DataByteArray.from(0x5F))
                delay(STANDARD_DELAY)
            }
        }
    }

    private suspend fun send(characteristics: ServerBleGattCharacteristic, data: DataByteArray) {
        characteristics.setValueAndNotifyClient(data)
    }
}