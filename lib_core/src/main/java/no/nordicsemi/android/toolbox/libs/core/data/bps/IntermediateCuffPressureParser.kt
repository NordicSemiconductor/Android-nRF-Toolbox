package no.nordicsemi.android.toolbox.libs.core.data.bps

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.FloatFormat
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.toolbox.libs.core.data.hts.DateTimeParser
import java.util.Calendar

object IntermediateCuffPressureParser {

    fun parse(data: ByteArray): IntermediateCuffPressureData? {
        val bytes = DataByteArray(data)
        if (bytes.size < 7) {
            return null
        }

        // First byte: flags
        var offset = 0
        val flags: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset++) ?: return null

        // See UNIT_* for unit options
        val unit: BloodPressureType = if (flags and 0x01 == BloodPressureType.UNIT_MMHG.value) {
            BloodPressureType.UNIT_MMHG
        } else {
            BloodPressureType.UNIT_KPA
        }
        val timestampPresent = flags and 0x02 != 0
        val pulseRatePresent = flags and 0x04 != 0
        val userIdPresent = flags and 0x08 != 0
        val measurementStatusPresent = flags and 0x10 != 0

        if (bytes.size < (7
                    + (if (timestampPresent) 7 else 0) + (if (pulseRatePresent) 2 else 0)
                    + (if (userIdPresent) 1 else 0) + if (measurementStatusPresent) 2 else 0)
        ) {
            return null
        }

        // Following bytes - systolic, diastolic and mean arterial pressure
        val cuffPressure: Float = bytes.getFloatValue(FloatFormat.FORMAT_SFLOAT, offset) ?: return null
        // final float ignored_1 = data.getFloatValue(Data.FORMAT_SFLOAT, offset + 2);
        // final float ignored_2 = data.getFloatValue(Data.FORMAT_SFLOAT, offset + 4);
        offset += 6

        // Parse timestamp if present
        var calendar: Calendar? = null
        if (timestampPresent) {
            calendar = DateTimeParser.parse(bytes, offset)
            offset += 7
        }

        // Parse pulse rate if present
        var pulseRate: Float? = null
        if (pulseRatePresent) {
            pulseRate = bytes.getFloatValue(FloatFormat.FORMAT_SFLOAT, offset)
            offset += 2
        }

        // Read user id if present
        var userId: Int? = null
        if (userIdPresent) {
            userId = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset)
            offset += 1
        }

        // Read measurement status if present
        var status: BPMStatus? = null
        if (measurementStatusPresent) {
            val measurementStatus: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset) ?: return null
            // offset += 2;
            status = BPMStatus(measurementStatus)
        }

        return IntermediateCuffPressureData(cuffPressure, unit, pulseRate, userId, status, calendar)
    }
}