package no.nordicsemi.android.toolbox.libs.core.data.rscs

import no.nordicsemi.kotlin.data.IntFormat
import no.nordicsemi.kotlin.data.getInt
import java.nio.ByteOrder

object RSCSDataParser {

    fun parse(data: ByteArray, byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN): RSCSData? {
        if (data.size < 4) return null

        var offset = 0

        // Flag
        val flags: Int = data.getInt(offset, IntFormat.UINT8).also { offset += 1 }
        val instantaneousStrideLengthPresent = flags and 0x01 != 0
        val totalDistancePresent = flags and 0x02 != 0
        val statusRunning = flags and 0x04 != 0

        // Speed
        val speed = data.getInt(offset, IntFormat.UINT16, byteOrder)
            .toFloat()
            .let {
                it / 256f // [m/s]
            }.also { offset += 2 }

        // Cadence
        val cadence: Int = data.getInt(offset, IntFormat.UINT8)
            .also { offset += 1 }

        // Check if the data size is correct.
        if (data.size < (4 + (if (instantaneousStrideLengthPresent) 2 else 0) + if (totalDistancePresent) 4 else 0)) {
            return null
        }

        // Stride length
        var strideLength: Int? = null
        if (instantaneousStrideLengthPresent) {
            strideLength =
                data.getInt(offset, IntFormat.UINT16, byteOrder)
                    .also { offset += 2 }
        }

        // Total distance
        var totalDistance: Long? = null
        if (totalDistancePresent) {
            totalDistance =
                data.getInt(offset, IntFormat.UINT32, byteOrder).toLong()
            // offset += 4;
        }

        return RSCSData(statusRunning, speed, cadence, strideLength, totalDistance)
    }
}
