package no.nordicsemi.android.service.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.lib.profile.throughput.ThroughputDataParser
import no.nordicsemi.android.service.repository.ThroughputRepository
import no.nordicsemi.android.toolbox.lib.utils.tryOrLog
import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.toolbox.libs.core.data.WritingStatus
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.core.WriteType
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

        fun writeRequest(
            deviceId: String,
            scope: CoroutineScope,
            maxWriteValueLength: Int,
            data: ByteArray,
        ) {
            scope.launch {
                try {
                    ThroughputRepository.updateWriteStatus(deviceId, WritingStatus.IN_PROGRESS)
                    coroutineScope {
                        val writeJobs = chunkData(data, maxWriteValueLength).map { chunk ->
                            async(Dispatchers.IO) {
                                writeCharacteristicProperty.write(
                                    data = chunk,
                                    writeType = WriteType.WITHOUT_RESPONSE
                                )
                            }
                        }
                        writeJobs.awaitAll()
                    }
                } catch (e: Exception) {
                    Timber.tag("ThroughputService").e("Error ${e.message}")
                }
                Timber.tag("ThroughputService").d("Writing data of ${data.size} bytes completed.")
                readThroughputMetrics(deviceId)
                ThroughputRepository.updateWriteStatus(deviceId, WritingStatus.COMPLETED)
            }
        }

        private suspend fun readThroughputMetrics(deviceId: String) {
            // Read data after write operation is complete
            val readData = writeCharacteristicProperty.read()
            // Parse the read data
            ThroughputDataParser.parse(data = readData)?.let {
                ThroughputRepository.updateThroughput(deviceId, it)
            }
        }

        fun resetData(deviceId: String, scope: CoroutineScope) {
            scope.launch {
                tryOrLog {
                    ThroughputRepository.updateWriteStatus(deviceId, WritingStatus.IN_PROGRESS)
                    writeCharacteristicProperty.write(
                        data = byteArrayOf(0x3D),
                        writeType = WriteType.WITHOUT_RESPONSE
                    )
                    Timber.tag("ThroughputService").d("Reset Completed.")
                }
                readThroughputMetrics(deviceId)
                ThroughputRepository.updateWriteStatus(deviceId, WritingStatus.COMPLETED)
            }
        }

        private fun chunkData(data: ByteArray, chunkSize: Int): List<ByteArray> {
            return data.toList()
                .chunked(chunkSize) { it.toByteArray() } // Efficient chunking
        }
    }

}
