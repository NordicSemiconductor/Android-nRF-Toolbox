package no.nordicsemi.android.lib.profile.directionFinder.distance

import no.nordicsemi.android.lib.profile.directionFinder.AddressType
import no.nordicsemi.android.lib.profile.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.kotlin.data.IntFormat
import no.nordicsemi.kotlin.data.getInt
import java.nio.ByteOrder
import kotlin.experimental.and

class DistanceMeasurementDataParser {

    fun parse(
        data: ByteArray,
        byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN
    ): DistanceMeasurementData? {
        if (data.size < 10) return null

        var offset = 0
        val flags = data[offset].also { offset++ }
        val isRTTPresent = flags and 0x01 > 0
        val isMCPDPresent = flags and 0x02 > 0
        val qualityIndicator = data.getInt(offset++, IntFormat.UINT8)

        val address = StringBuilder().apply {
            for (i in 0..5) {
                data.getInt(offset++, IntFormat.UINT8).let {
                    insert(0, Integer.toHexString(it))
                    if (i != 5) insert(0, ":")
                }
            }
        }.toString()

        val addressType = data.getInt(offset++, IntFormat.UINT8)

        val rtt = if (isRTTPresent) {
            RTTEstimate(data.getInt(offset, IntFormat.UINT16, byteOrder)).also { offset += 2 }
        } else null

        val mcpd = if (isMCPDPresent) {
            MCPDEstimate(
                data.getInt(offset, IntFormat.UINT16, byteOrder).also { offset += 2 },
                data.getInt(offset, IntFormat.UINT16, byteOrder).also { offset += 2 },
                data.getInt(offset, IntFormat.UINT16, byteOrder).also { offset += 2 },
                data.getInt(offset, IntFormat.UINT16, byteOrder).also { offset += 2 },
            )
        } else null

        val result = if (isRTTPresent) {
            RttMeasurementData(
                flags,
                QualityIndicator.create(qualityIndicator),
                PeripheralBluetoothAddress(AddressType.create(addressType), address),
                rtt!!
            )
        } else {
            McpdMeasurementData(
                flags,
                QualityIndicator.create(qualityIndicator),
                PeripheralBluetoothAddress(AddressType.create(addressType), address),
                mcpd!!,
            )
        }

        return result
    }
}
