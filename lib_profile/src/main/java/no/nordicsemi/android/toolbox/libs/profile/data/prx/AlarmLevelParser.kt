package no.nordicsemi.android.toolbox.libs.profile.data.prx

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat

object AlarmLevelParser {

    fun parse(data: ByteArray): AlarmLevel? {
        val bytes = DataByteArray(data)
        if (bytes.size == 1) {
            val level: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 0) ?: return null
            return AlarmLevel.create(level)
        }
        return null
    }
}