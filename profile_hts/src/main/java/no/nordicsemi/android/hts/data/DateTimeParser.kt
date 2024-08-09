package no.nordicsemi.android.hts.data

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import java.util.Calendar

internal object DateTimeParser {

    fun parse(bytes: DataByteArray, offset: Int): Calendar? {
        if (bytes.size < offset + 7) return null
        val calendar = Calendar.getInstance()
        val year: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset) ?: return null
        val month: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset + 2) ?: return null
        val day: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset + 3) ?: return null
        val hourOfDay: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset + 4) ?: return null
        val minute: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset + 5) ?: return null
        val second: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset + 6) ?: return null

        if (year > 0) {
            calendar[Calendar.YEAR] = year
        } else {
            calendar.clear(Calendar.YEAR)
        }

        if (month > 0) {
            calendar[Calendar.MONTH] = month - 1
        } else {
            calendar.clear(Calendar.MONTH)
        }

        if (day > 0) {
            calendar[Calendar.DATE] = day
        } else {
            calendar.clear(Calendar.DATE)
        }

        calendar[Calendar.HOUR_OF_DAY] = hourOfDay
        calendar[Calendar.MINUTE] = minute
        calendar[Calendar.SECOND] = second
        calendar[Calendar.MILLISECOND] = 0

        return calendar
    }
}