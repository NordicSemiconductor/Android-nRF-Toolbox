package no.nordicsemi.android.hts.data

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.FloatFormat
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import java.util.Calendar

object HTSDataParser {

    fun parse(byte: ByteArray): HtsData? {
        val bytes = DataByteArray(byte)

        if (bytes.size < 5) {
            return null
        }

        var offset = 0
        val flags: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset) ?: return null

        val unit: TemperatureUnitData = TemperatureUnitData.create(flags and 0x01) ?: return null

        val timestampPresent = flags and 0x02 != 0
        val temperatureTypePresent = flags and 0x04 != 0
        offset += 1

        if (bytes.size < 5 + (if (timestampPresent) 7 else 0) + (if (temperatureTypePresent) 1 else 0)) {
            return null
        }

        val temperature: Float =
            bytes.getFloatValue(FloatFormat.FORMAT_FLOAT, offset) ?: return null
        offset += 4

        var calendar: Calendar? = null
        if (timestampPresent) {
            calendar = DateTimeParser.parse(bytes, offset)
            offset += 7
        }

        var type: Int? = null
        if (temperatureTypePresent) {
            type = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset)
            // offset += 1;
        }

        return HtsData(temperature, unit, calendar, type)
    }
}
