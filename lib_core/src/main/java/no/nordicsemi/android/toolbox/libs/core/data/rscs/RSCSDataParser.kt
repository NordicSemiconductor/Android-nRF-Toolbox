package no.nordicsemi.android.toolbox.libs.core.data.rscs

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.kotlin.ble.core.data.util.LongFormat

object RSCSDataParser {

    fun parse(data: ByteArray): RSCSData? {
        val bytes = DataByteArray(data)
        if (bytes.size < 4) {
            return null
        }

        var offset = 0
        val flags: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset) ?: return null
        val instantaneousStrideLengthPresent = flags and 0x01 != 0
        val totalDistancePresent = flags and 0x02 != 0
        val statusRunning = flags and 0x04 != 0
        offset += 1

        val speed = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset)?.toFloat()?.let {
            it / 256f // [m/s]
        } ?: return null

        offset += 2
        val cadence: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset) ?: return null
        offset += 1

        if (bytes.size < (4 + (if (instantaneousStrideLengthPresent) 2 else 0) + if (totalDistancePresent) 4 else 0)) {
            return null
        }

        var strideLength: Int? = null
        if (instantaneousStrideLengthPresent) {
            strideLength = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset)
            offset += 2
        }

        var totalDistance: Long? = null
        if (totalDistancePresent) {
            totalDistance = bytes.getLongValue(LongFormat.FORMAT_UINT32_LE, offset)
            // offset += 4;
        }

        return RSCSData(statusRunning, speed, cadence, strideLength, totalDistance)
    }
}
