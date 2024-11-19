package no.nordicsemi.android.toolbox.libs.core.data.directionFinder.ddf

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import kotlin.experimental.and

class DDFDataParser {

    fun parse(data: ByteArray): DDFData? {
        val bytes = DataByteArray(data)
        if (bytes.size < 1) {
            return null
        }

        var offset = 0
        val flags = bytes.getByte(offset).also { offset++ }
            ?: throw IllegalArgumentException("Byte at offset $offset is null")

        val isRTTPresent = flags and 0x01 > 0
        val isMCPDPresent = flags and 0x02 > 0

        return DDFData(isMCPDPresent, isRTTPresent)
    }
}
