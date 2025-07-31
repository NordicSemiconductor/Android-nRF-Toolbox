package no.nordicsemi.android.lib.profile.hrs

import no.nordicsemi.kotlin.data.IntFormat
import no.nordicsemi.kotlin.data.getInt
import java.nio.ByteOrder

object BodySensorLocationParser {

    fun parse(bytes: ByteArray): Int? {
        if (bytes.isEmpty()) return null

        return bytes.getInt(0, IntFormat.UINT8, ByteOrder.LITTLE_ENDIAN)
    }
}