package no.nordicsemi.android.toolbox.libs.core.data.hrs

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat

object HRSDataParser {

    fun parse(data: ByteArray): HRSData? {
        val bytes = DataByteArray(data)

        if (bytes.size < 2) {
            return null
        }
        // Read flags
        var offset = 0
        val flags: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset) ?: return null
        val hearRateType: IntFormat = if (flags and 0x01 == 0) {
            IntFormat.FORMAT_UINT8
        } else {
            IntFormat.FORMAT_UINT16_LE
        }
        val sensorContactStatus = flags and 0x06 shr 1
        val sensorContactSupported = sensorContactStatus == 2 || sensorContactStatus == 3
        val sensorContactDetected = sensorContactStatus == 3
        val energyExpandedPresent = flags and 0x08 != 0
        val rrIntervalsPresent = flags and 0x10 != 0
        offset += 1

        // Validate packet length
        if (bytes.size < (1 + (hearRateType.value and 0x0F) + (if (energyExpandedPresent) 2 else 0) + if (rrIntervalsPresent) 2 else 0)) {
            return null
        }

        // Prepare data
        val sensorContact = if (sensorContactSupported) sensorContactDetected else false

        val heartRate: Int = bytes.getIntValue(hearRateType, offset) ?: return null
        offset += hearRateType.value and 0xF

        var energyExpanded: Int? = null
        if (energyExpandedPresent) {
            energyExpanded = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset)
            offset += 2
        }

        val rrIntervals = if (rrIntervalsPresent) {
            val count: Int = (bytes.size - offset) / 2
            val intervals: MutableList<Int> = ArrayList(count)
            for (i in 0 until count) {
                intervals.add(bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset)!!)
                offset += 2
            }
            intervals.toList()
        } else {
            emptyList()
        }

        return HRSData(heartRate, sensorContact, energyExpanded, rrIntervals)
    }
}
