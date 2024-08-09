package no.nordicsemi.android.hts.data

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat

object BatteryLevelParser {

    fun parse(byte: ByteArray): Int? {
        val bytes = DataByteArray(byte)
        if (bytes.size == 1) {
            return bytes.getIntValue(IntFormat.FORMAT_UINT8, 0)
        }
        return null
    }
}