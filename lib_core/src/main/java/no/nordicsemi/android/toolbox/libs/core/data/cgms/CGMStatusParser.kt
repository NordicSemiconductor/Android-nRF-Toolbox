package no.nordicsemi.android.toolbox.libs.core.data.cgms

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.toolbox.libs.core.data.cgms.data.CGMStatus
import no.nordicsemi.android.toolbox.libs.core.data.cgms.data.CGMStatusEnvelope
import no.nordicsemi.android.toolbox.libs.core.data.common.CRC16

object CGMStatusParser {

    fun parse(data: ByteArray): CGMStatusEnvelope? {
        val bytes = DataByteArray(data)
        if (bytes.size != 5 && bytes.size != 7) {
            return null
        }

        val timeOffset: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, 0) ?: return null
        val warningStatus: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 2) ?: return null
        val calibrationTempStatus: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 3) ?: return null
        val sensorStatus: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 4) ?: return null

        val crcPresent = bytes.size == 7
        if (crcPresent) {
            val actualCrc: Int = CRC16.MCRF4XX(bytes.value, 0, 5)
            val expectedCrc: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, 5) ?: return null
            if (actualCrc != expectedCrc) {
                return null
            }
        }

        val status = CGMStatus(warningStatus, calibrationTempStatus, sensorStatus)
        return CGMStatusEnvelope(status, timeOffset, crcPresent, crcPresent)
    }
}
