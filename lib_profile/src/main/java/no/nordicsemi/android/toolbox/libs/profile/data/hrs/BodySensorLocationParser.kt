package no.nordicsemi.android.toolbox.libs.profile.data.hrs

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat

object BodySensorLocationParser {

    fun parse(bytes: ByteArray): Int? {
        val data = DataByteArray(bytes)

        if (data.size < 1) {
            return null
        }

        return data.getIntValue(IntFormat.FORMAT_UINT8, 0)
    }
}