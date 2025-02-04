package no.nordicsemi.android.service.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.lib.profile.throughput.ThroughputDataParser
import no.nordicsemi.android.service.repository.ThroughputRepository
import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.toolbox.libs.core.data.NumberOfBytes
import no.nordicsemi.android.toolbox.libs.core.data.NumberOfSeconds
import no.nordicsemi.android.toolbox.libs.core.data.ThroughputInputType
import no.nordicsemi.android.toolbox.libs.core.data.WritingStatus
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.core.WriteType
import no.nordicsemi.kotlin.ble.core.util.chunked
import timber.log.Timber
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

private val THROUGHPUT_CHAR_UUID = UUID.fromString("00001524-0000-1000-8000-00805F9B34FB")

internal class ThroughputManager : ServiceManager {
    override val profile: Profile
        get() = Profile.THROUGHPUT

    @OptIn(ExperimentalUuidApi::class)
    override fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        scope.launch {
            remoteService.characteristics.firstOrNull { it.uuid == THROUGHPUT_CHAR_UUID.toKotlinUuid() }
                ?.also { writeCharacteristicProperty = it }
        }
    }

    companion object {
        private lateinit var writeCharacteristicProperty: RemoteCharacteristic

        suspend fun writeRequest(
            deviceId: String,
            maxWriteValueLength: Int,
            inputType: ThroughputInputType,
        ) {
            try {
                ThroughputRepository.updateWriteStatus(deviceId, WritingStatus.IN_PROGRESS)
                when (inputType) {
                    is NumberOfBytes -> {
                        writeBytesData(maxWriteValueLength, inputType.numberOfBytes)
                    }

                    is NumberOfSeconds -> {
                        writeTimesData(maxWriteValueLength, inputType.numberOfSeconds)
                    }
                }
            } catch (e: Exception) {
                Timber.tag("ThroughputService").e("Error ${e.message}")
            } finally {
                readThroughputMetrics(deviceId)
                ThroughputRepository.updateWriteStatus(deviceId, WritingStatus.COMPLETED)
            }
        }

        private suspend fun writeBytesData(
            maxWriteValueLength: Int,
            numberOfBytes: Int
        ) {
            val array = ByteArray(numberOfBytes) { 0x3D }
            writeCharacteristicProperty.write(
                data = byteArrayOf(0x3D),
                writeType = WriteType.WITHOUT_RESPONSE
            )
            array.chunked(maxWriteValueLength).map {
                writeCharacteristicProperty.write(
                    data = it,
                    writeType = WriteType.WITHOUT_RESPONSE
                )
            }
        }

        private suspend fun writeTimesData(
            maxWriteValueLength: Int,
            numberOfSecond: Int
        ) {
            val array = ByteArray(maxWriteValueLength) { 0x3D }
            val startTime = System.currentTimeMillis()
            writeCharacteristicProperty.write(
                data = byteArrayOf(0x3D),
                writeType = WriteType.WITHOUT_RESPONSE
            )
            while (System.currentTimeMillis() - startTime < numberOfSecond * 1000) {
                writeCharacteristicProperty.write(
                    data = array,
                    writeType = WriteType.WITHOUT_RESPONSE
                )
            }
        }

        private suspend fun readThroughputMetrics(deviceId: String) {
            try {
                // Read data after write operation is complete
                val readData = writeCharacteristicProperty.read()

                // Parse the read data
                ThroughputDataParser.parse(data = readData)?.let {
                    ThroughputRepository.updateThroughput(deviceId, it)
                }
            } catch (e: Exception) {
                Timber.tag("ThroughputService").e("Error ${e.message}")
            }
        }

    }

}

