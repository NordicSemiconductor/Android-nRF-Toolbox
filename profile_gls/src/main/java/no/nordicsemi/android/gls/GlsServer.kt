package no.nordicsemi.android.gls

import android.annotation.SuppressLint
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.core.DataByteArray
import no.nordicsemi.android.common.logger.BleLogger
import no.nordicsemi.android.common.logger.DefaultConsoleLogger
import no.nordicsemi.android.gls.main.viewmodel.BATTERY_LEVEL_CHARACTERISTIC_UUID
import no.nordicsemi.android.gls.main.viewmodel.BATTERY_SERVICE_UUID
import no.nordicsemi.android.gls.main.viewmodel.GLS_SERVICE_UUID
import no.nordicsemi.android.gls.main.viewmodel.GLUCOSE_MEASUREMENT_CHARACTERISTIC
import no.nordicsemi.android.gls.main.viewmodel.GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC
import no.nordicsemi.android.gls.main.viewmodel.RACP_CHARACTERISTIC
import no.nordicsemi.android.kotlin.ble.advertiser.BleAdvertiser
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertisingConfig
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.profile.gls.RecordAccessControlPointInputParser
import no.nordicsemi.android.kotlin.ble.server.main.ServerBleGatt
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattCharacteristicConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServiceConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServiceType
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBluetoothGattConnection
import javax.inject.Inject
import javax.inject.Singleton

private const val STANDARD_DELAY = 1000L

@SuppressLint("MissingPermission")
@Singleton
class GlsServer @Inject constructor(
    private val scope: CoroutineScope,
    @ApplicationContext
    private val context: Context,
    private val logger: BleLogger = DefaultConsoleLogger(context)
) {

    lateinit var server: ServerBleGatt

    lateinit var glsCharacteristic: ServerBleGattCharacteristic
    lateinit var glsContextCharacteristic: ServerBleGattCharacteristic
    lateinit var racpCharacteristic: ServerBleGattCharacteristic
    lateinit var batteryLevelCharacteristic: ServerBleGattCharacteristic

    private var lastRequest = DataByteArray()

    val YOUNGEST_RECORD = DataByteArray.from(
        0x07,
        0x00,
        0x00,
        0xDC.toByte(),
        0x07,
        0x01,
        0x01,
        0x0C,
        0x1E,
        0x05,
        0x00,
        0x00,
        0x26,
        0xD2.toByte(),
        0x11
    )
    val OLDEST_RECORD = DataByteArray.from(
        0x07,
        0x04,
        0x00,
        0xDC.toByte(),
        0x07,
        0x01,
        0x01,
        0x0C,
        0x1E,
        0x11,
        0x00,
        0x00,
        0x82.toByte(),
        0xD2.toByte(),
        0x11
    )

    val records = listOf(
        YOUNGEST_RECORD,
        DataByteArray.from(
            0x07,
            0x01,
            0x00,
            0xDC.toByte(),
            0x07,
            0x01,
            0x01,
            0x0C,
            0x1E,
            0x08,
            0x00,
            0x00,
            0x3D,
            0xD2.toByte(),
            0x11
        ),
        DataByteArray.from(
            0x07,
            0x02,
            0x00,
            0xDC.toByte(),
            0x07,
            0x01,
            0x01,
            0x0C,
            0x1E,
            0x0B,
            0x00,
            0x00,
            0x54,
            0xD2.toByte(),
            0x11
        ),
        DataByteArray.from(
            0x07,
            0x03,
            0x00,
            0xDC.toByte(),
            0x07,
            0x01,
            0x01,
            0x0C,
            0x1E,
            0x0E,
            0x00,
            0x00,
            0x6B,
            0xD2.toByte(),
            0x11
        ),
        OLDEST_RECORD
    )

    val racp = DataByteArray.from(0x06, 0x00, 0x01, 0x01)

    fun start(
        context: Context,
        device: MockServerDevice = MockServerDevice(
            name = "GLS Server",
            address = "55:44:33:22:11"
        ),
    ) = scope.launch {
        val gmCharacteristic = ServerBleGattCharacteristicConfig(
            GLUCOSE_MEASUREMENT_CHARACTERISTIC,
            listOf(BleGattProperty.PROPERTY_NOTIFY),
            listOf()
        )

        val gmContextCharacteristic = ServerBleGattCharacteristicConfig(
            GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC,
            listOf(BleGattProperty.PROPERTY_NOTIFY),
            listOf()
        )

        val racpCharacteristic = ServerBleGattCharacteristicConfig(
            RACP_CHARACTERISTIC,
            listOf(BleGattProperty.PROPERTY_INDICATE, BleGattProperty.PROPERTY_WRITE),
            listOf(BleGattPermission.PERMISSION_WRITE)
        )

        val serviceConfig = ServerBleGattServiceConfig(
            GLS_SERVICE_UUID,
            ServerBleGattServiceType.SERVICE_TYPE_PRIMARY,
            listOf(gmCharacteristic, gmContextCharacteristic, racpCharacteristic)
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
            config = arrayOf(serviceConfig, batteryService),
            mock = device,
            logger = { priority, log -> println(log) }
        )

        val advertiser = BleAdvertiser.create(context)
        advertiser.advertise(config = BleAdvertisingConfig(), mock = device).launchIn(scope)

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
        val glsService = connection.services.findService(GLS_SERVICE_UUID)!!
        glsCharacteristic = glsService.findCharacteristic(GLUCOSE_MEASUREMENT_CHARACTERISTIC)!!
        glsContextCharacteristic = glsService.findCharacteristic(
            GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC
        )!!
        racpCharacteristic = glsService.findCharacteristic(RACP_CHARACTERISTIC)!!

        val batteryService = connection.services.findService(BATTERY_SERVICE_UUID)!!
        batteryLevelCharacteristic = batteryService.findCharacteristic(
            BATTERY_LEVEL_CHARACTERISTIC_UUID
        )!!


        startGlsService(connection)
//        startBatteryService(connection)
    }

    private fun startGlsService(connection: ServerBluetoothGattConnection) {
        racpCharacteristic.value
            .onEach { lastRequest = it }
            .launchIn(scope)
    }

    internal fun continueWithResponse() {
        sendResponse(lastRequest)
    }

    private fun sendResponse(request: DataByteArray) {
        if (request == RecordAccessControlPointInputParser.reportNumberOfAllStoredRecords()) {
            sendAll(glsCharacteristic)
            racpCharacteristic.setValue(racp)
        } else if (request == RecordAccessControlPointInputParser.reportLastStoredRecord()) {
            sendLast(glsCharacteristic)
            racpCharacteristic.setValue(racp)
        } else if (request == RecordAccessControlPointInputParser.reportFirstStoredRecord()) {
            sendFirst(glsCharacteristic)
            racpCharacteristic.setValue(racp)
        }
    }

    private fun sendFirst(characteristics: ServerBleGattCharacteristic) {
        characteristics.setValue(records.first())
    }

    private fun sendLast(characteristics: ServerBleGattCharacteristic) {
        characteristics.setValue(records.last())
    }

    private fun sendAll(characteristics: ServerBleGattCharacteristic) = scope.launch {
        records.forEach {
            characteristics.setValue(it)
            delay(100)
        }
    }

    private fun startBatteryService(connection: ServerBluetoothGattConnection) {
        scope.launch {
            repeat(100) {
                batteryLevelCharacteristic.setValue(DataByteArray.from(0x61))
                delay(STANDARD_DELAY)
                batteryLevelCharacteristic.setValue(DataByteArray.from(0x60))
                delay(STANDARD_DELAY)
                batteryLevelCharacteristic.setValue(DataByteArray.from(0x5F))
                delay(STANDARD_DELAY)
            }
        }
    }
}