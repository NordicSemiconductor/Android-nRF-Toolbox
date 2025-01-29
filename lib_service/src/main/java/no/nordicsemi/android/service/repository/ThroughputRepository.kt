package no.nordicsemi.android.service.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.lib.profile.throughput.ThroughputMetrics
import no.nordicsemi.android.service.services.ThroughputManager
import no.nordicsemi.android.toolbox.libs.core.data.ThroughputServiceData
import no.nordicsemi.android.toolbox.libs.core.data.WriteDataType

object ThroughputRepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<ThroughputServiceData>>()

    fun getData(deviceId: String): StateFlow<ThroughputServiceData> =
        _dataMap.getOrPut(deviceId) { MutableStateFlow(ThroughputServiceData()) }


    fun sendDataToDK(
        deviceId: String,
        scope: CoroutineScope,
        data: String,
        writeDataType: WriteDataType,
    ) {
        val isHighestMtuRequested = _dataMap[deviceId]?.value?.isHighestMtuRequested ?: false
        if (data.isNotEmpty()) {
            ThroughputManager.writeRequest(
                deviceId = deviceId,
                scope = scope,
                isHighestMtuRequested = isHighestMtuRequested,
                data = convertToByteArray(data, writeDataType),
            )
        } else {
            ThroughputManager.writeRequest(
                deviceId = deviceId,
                scope = scope,
                isHighestMtuRequested = isHighestMtuRequested,
            )
        }
    }

    fun resetData(deviceId: String, scope: CoroutineScope) {
        ThroughputManager.resetData(deviceId, scope)
    }

    fun updateThroughput(deviceId: String, throughputMetrics: ThroughputMetrics) {
        _dataMap[deviceId]?.update {
            it.copy(throughputData = throughputMetrics)
        }
    }

    fun mtuRequested(address: String) {
        _dataMap[address]?.update {
            it.copy(isHighestMtuRequested = true)
        }
    }

    private fun convertToByteArray(data: String, writeDataType: WriteDataType): ByteArray {
        return when (writeDataType) {
            WriteDataType.TEXT -> stringToByteArray(data)
            WriteDataType.HEX -> hexStringToByteArray(data)
            WriteDataType.ASCII -> asciiStringToByteArray(data)
        }
    }

    private fun stringToByteArray(data: String): ByteArray = data.encodeToByteArray()

    private fun hexStringToByteArray(hex: String): ByteArray {
        require(hex.length % 2 == 0) { "Hex string must have an even length" }

        return ByteArray(hex.length / 2) { index ->
            hex.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }

    private fun asciiStringToByteArray(hexString: String): ByteArray {
        return hexString.split(",") // Split by commas
            .map { it.trim().removePrefix("0x").toInt(16).toByte() } // Convert hex to byte
            .toByteArray()
    }

}