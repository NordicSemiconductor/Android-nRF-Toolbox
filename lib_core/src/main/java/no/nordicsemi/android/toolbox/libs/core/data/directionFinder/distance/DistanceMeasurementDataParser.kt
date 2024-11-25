package no.nordicsemi.android.toolbox.libs.core.data.directionFinder.distance

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.AddressType
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.PeripheralBluetoothAddress
import kotlin.experimental.and

class DistanceMeasurementDataParser {

    fun parse(data: ByteArray): DistanceMeasurementData? {
        val bytes = DataByteArray(data)

        if (bytes.size < 10) return null

        var offset = 0
        val flags = bytes.getByte(offset).also { offset++ }
            ?: throw IllegalArgumentException("Byte at offset $offset is null")
        val isRTTPresent = flags and 0x01 > 0
        val isMCPDPresent = flags and 0x02 > 0
        val qualityIndicator = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset).also { offset++ }

        val address = StringBuilder().apply {
            for (i in 0..5) {
                bytes.getIntValue(IntFormat.FORMAT_UINT8, offset++)?.let {
                    insert(0, Integer.toHexString(it))
                    if (i != 5) insert(0, ":")
                }
            }
        }.toString()

        val addressType = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset).also { offset++ }


        val rtt = if (isRTTPresent) {
            RTTEstimate(
                bytes.getIntValue(
                    IntFormat.FORMAT_UINT16_LE,
                    offset
                )!!
            ).also { offset += 2 }
        } else {
            null
        }

        val mcpd = if (isMCPDPresent) {
            MCPDEstimate(
                bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset)!!.also { offset += 2 },
                bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset)!!.also { offset += 2 },
                bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset)!!.also { offset += 2 },
                bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset)!!.also { offset += 2 },
            )
        } else {
            null
        }

        val result = if (isRTTPresent) {
            RttMeasurementData(
                flags,
                QualityIndicator.create(qualityIndicator!!),
                PeripheralBluetoothAddress(AddressType.create(addressType!!), address),
                rtt!!
            )
        } else {
            McpdMeasurementData(
                flags,
                QualityIndicator.create(qualityIndicator!!),
                PeripheralBluetoothAddress(AddressType.create(addressType!!), address),
                mcpd!!,
            )
        }

        return result
    }
}
