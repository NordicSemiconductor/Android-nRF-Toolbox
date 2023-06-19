package no.nordicsemi.android.gls

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.gls.main.viewmodel.BATTERY_LEVEL_CHARACTERISTIC_UUID
import no.nordicsemi.android.gls.main.viewmodel.BATTERY_SERVICE_UUID
import no.nordicsemi.android.gls.main.viewmodel.GLS_SERVICE_UUID
import no.nordicsemi.android.gls.main.viewmodel.GLUCOSE_MEASUREMENT_CHARACTERISTIC
import no.nordicsemi.android.gls.main.viewmodel.GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC
import no.nordicsemi.android.gls.main.viewmodel.RACP_CHARACTERISTIC
import no.nordicsemi.android.kotlin.ble.advertiser.BleAdvertiser
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertiseConfig
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.profile.gls.RecordAccessControlPointInputParser
import no.nordicsemi.android.kotlin.ble.server.main.BleGattServer
import no.nordicsemi.android.kotlin.ble.server.main.service.BleGattServerServiceType
import no.nordicsemi.android.kotlin.ble.server.main.service.BleServerGattCharacteristic
import no.nordicsemi.android.kotlin.ble.server.main.service.BleServerGattCharacteristicConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.BleServerGattServiceConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.BluetoothGattServerConnection
import javax.inject.Inject
import javax.inject.Singleton

private const val STANDARD_DELAY = 1000L

@SuppressLint("MissingPermission")
@Singleton
class GlsServer @Inject constructor(
    private val scope: CoroutineScope
) {

    lateinit var server: BleGattServer

    lateinit var glsCharacteristic: BleServerGattCharacteristic
    lateinit var glsContextCharacteristic: BleServerGattCharacteristic
    lateinit var racpCharacteristic: BleServerGattCharacteristic
    lateinit var batteryLevelCharacteristic: BleServerGattCharacteristic

    private var lastRequest = byteArrayOf()

    val YOUNGEST_RECORD = byteArrayOf(0x07, 0x00, 0x00, 0xDC.toByte(), 0x07, 0x01, 0x01, 0x0C, 0x1E, 0x05, 0x00, 0x00, 0x26, 0xD2.toByte(), 0x11)
    val OLDEST_RECORD = byteArrayOf(0x07, 0x04, 0x00, 0xDC.toByte(), 0x07, 0x01, 0x01, 0x0C, 0x1E, 0x11, 0x00, 0x00, 0x82.toByte(), 0xD2.toByte(), 0x11)

    val records = listOf(
        YOUNGEST_RECORD,
        byteArrayOf(0x07, 0x01, 0x00, 0xDC.toByte(), 0x07, 0x01, 0x01, 0x0C, 0x1E, 0x08, 0x00, 0x00, 0x3D, 0xD2.toByte(), 0x11),
        byteArrayOf(0x07, 0x02, 0x00, 0xDC.toByte(), 0x07, 0x01, 0x01, 0x0C, 0x1E, 0x0B, 0x00, 0x00, 0x54, 0xD2.toByte(), 0x11),
        byteArrayOf(0x07, 0x03, 0x00, 0xDC.toByte(), 0x07, 0x01, 0x01, 0x0C, 0x1E, 0x0E, 0x00, 0x00, 0x6B, 0xD2.toByte(), 0x11),
        OLDEST_RECORD
    )

    val racp = byteArrayOf(0x06, 0x00, 0x01, 0x01)

    fun start(
        context: Context,
        device: MockServerDevice = MockServerDevice(
            name = "GLS Server",
            address = "55:44:33:22:11"
        ),
    ) = scope.launch {
        val gmCharacteristic = BleServerGattCharacteristicConfig(
            GLUCOSE_MEASUREMENT_CHARACTERISTIC,
            listOf(BleGattProperty.PROPERTY_NOTIFY),
            listOf()
        )

        val gmContextCharacteristic = BleServerGattCharacteristicConfig(
            GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC,
            listOf(BleGattProperty.PROPERTY_NOTIFY),
            listOf()
        )

        val racpCharacteristic = BleServerGattCharacteristicConfig(
            RACP_CHARACTERISTIC,
            listOf(BleGattProperty.PROPERTY_INDICATE, BleGattProperty.PROPERTY_WRITE),
            listOf(BleGattPermission.PERMISSION_WRITE)
        )

        val serviceConfig = BleServerGattServiceConfig(
            GLS_SERVICE_UUID,
            BleGattServerServiceType.SERVICE_TYPE_PRIMARY,
            listOf(gmCharacteristic, gmContextCharacteristic, racpCharacteristic)
        )

        val batteryLevelCharacteristic = BleServerGattCharacteristicConfig(
            BATTERY_LEVEL_CHARACTERISTIC_UUID,
            listOf(BleGattProperty.PROPERTY_READ, BleGattProperty.PROPERTY_NOTIFY),
            listOf(BleGattPermission.PERMISSION_READ)
        )

        val batteryService = BleServerGattServiceConfig(
            BATTERY_SERVICE_UUID,
            BleGattServerServiceType.SERVICE_TYPE_PRIMARY,
            listOf(batteryLevelCharacteristic)
        )

        server = BleGattServer.create(
            context = context,
            config = arrayOf(serviceConfig, batteryService),
            mock = device
        )

        val advertiser = BleAdvertiser.create(context)
        advertiser.advertise(config = BleAdvertiseConfig(), mock = device).launchIn(scope)

        launch {
            server.connections
                .mapNotNull { it.values.firstOrNull() }
                .collect { setUpConnection(it) }
        }
    }

    internal fun stopServer() {
        server.stopServer()
    }

    private fun setUpConnection(connection: BluetoothGattServerConnection) {
        val glsService = connection.services.findService(GLS_SERVICE_UUID)!!
        glsCharacteristic = glsService.findCharacteristic(GLUCOSE_MEASUREMENT_CHARACTERISTIC)!!
        glsContextCharacteristic = glsService.findCharacteristic(GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC)!!
        racpCharacteristic = glsService.findCharacteristic(RACP_CHARACTERISTIC)!!

        val batteryService = connection.services.findService(BATTERY_SERVICE_UUID)!!
        batteryLevelCharacteristic = batteryService.findCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)!!


        startGlsService(connection)
//        startBatteryService(connection)
    }

    private fun startGlsService(connection: BluetoothGattServerConnection) {
        racpCharacteristic.value
            .filter { it.isNotEmpty() }
            .onEach { lastRequest = it }
            .launchIn(scope)
    }

    internal fun continueWithResponse() {
        sendResponse(lastRequest)
    }

    private fun sendResponse(request: ByteArray) {
        if (request.contentEquals(RecordAccessControlPointInputParser.reportNumberOfAllStoredRecords().value)) {
            sendAll(glsCharacteristic)
            racpCharacteristic.setValue(racp)
        } else if (request.contentEquals(RecordAccessControlPointInputParser.reportLastStoredRecord().value)) {
            sendLast(glsCharacteristic)
            racpCharacteristic.setValue(racp)
        } else if (request.contentEquals(RecordAccessControlPointInputParser.reportFirstStoredRecord().value)) {
            sendFirst(glsCharacteristic)
            racpCharacteristic.setValue(racp)
        }
    }

    private fun sendFirst(characteristics: BleServerGattCharacteristic) {
        characteristics.setValue(records.first())
    }

    private fun sendLast(characteristics: BleServerGattCharacteristic) {
        characteristics.setValue(records.last())
    }

    private fun sendAll(characteristics: BleServerGattCharacteristic) = scope.launch {
        records.forEach {
            characteristics.setValue(it)
            delay(100)
        }
    }

    private fun startBatteryService(connection: BluetoothGattServerConnection) {
        scope.launch {
            repeat(100) {
                batteryLevelCharacteristic.setValue(byteArrayOf(0x61))
                delay(STANDARD_DELAY)
                batteryLevelCharacteristic.setValue(byteArrayOf(0x60))
                delay(STANDARD_DELAY)
                batteryLevelCharacteristic.setValue(byteArrayOf(0x5F))
                delay(STANDARD_DELAY)
            }
        }
    }
}