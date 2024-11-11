package no.nordicsemi.android.toolbox.libs.core.data.cgms

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.FloatFormat
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.toolbox.libs.core.data.cgms.data.CGMRecord
import no.nordicsemi.android.toolbox.libs.core.data.cgms.data.CGMStatus
import no.nordicsemi.android.toolbox.libs.core.data.common.CRC16

object CGMMeasurementParser {

    fun parse(data: ByteArray): List<CGMRecord>? {
        val bytes = DataByteArray(data)
        if (bytes.size < 1) {
            return null
        }

        var offset = 0

        val result = mutableListOf<CGMRecord>()

        while (offset < bytes.size) {
            // Packet size
            val size: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset) ?: return null
            if (size < 6 || offset + size > bytes.size) {
                return null
            }

            // Flags
            val flags: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset + 1) ?: return null
            val cgmTrendInformationPresent = flags and 0x01 != 0
            val cgmQualityInformationPresent = flags and 0x02 != 0
            val sensorWarningOctetPresent = flags and 0x20 != 0
            val sensorCalTempOctetPresent = flags and 0x40 != 0
            val sensorStatusOctetPresent = flags and 0x80 != 0
            val dataSize =
                (6 + (if (cgmTrendInformationPresent) 2 else 0) + (if (cgmQualityInformationPresent) 2 else 0)
                        + (if (sensorWarningOctetPresent) 1 else 0) + (if (sensorCalTempOctetPresent) 1 else 0)
                        + if (sensorStatusOctetPresent) 1 else 0)
            if (size != dataSize && size != dataSize + 2) {
                return null
            }
            val crcPresent = size == dataSize + 2
            if (crcPresent) {
                val expectedCrc: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset + dataSize) ?: return null
                val actualCrc: Int = CRC16.MCRF4XX(bytes.value, offset, dataSize)
                if (expectedCrc != actualCrc) {
                    continue
                }
            }
            offset += 2

            // Glucose concentration
            val glucoseConcentration: Float = bytes.getFloatValue(FloatFormat.FORMAT_SFLOAT, offset) ?: return null
            offset += 2

            // Time offset (in minutes since Session Start)
            val timeOffset: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset) ?: return null
            offset += 2

            // Sensor Status Annunciation
            var warningStatus = 0
            var calibrationTempStatus = 0
            var sensorStatus = 0
            var status: CGMStatus? = null
            if (sensorWarningOctetPresent) {
                warningStatus = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset++) ?: return null
            }
            if (sensorCalTempOctetPresent) {
                calibrationTempStatus = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset++) ?: return null
            }
            if (sensorStatusOctetPresent) {
                sensorStatus = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset++) ?: return null
            }
            if (sensorWarningOctetPresent || sensorCalTempOctetPresent || sensorStatusOctetPresent) {
                status = CGMStatus(warningStatus, calibrationTempStatus, sensorStatus)
            }

            // CGM Trend Information
            var trend: Float? = null
            if (cgmTrendInformationPresent) {
                trend = bytes.getFloatValue(FloatFormat.FORMAT_SFLOAT, offset)
                offset += 2
            }

            // CGM Quality Information
            var quality: Float? = null
            if (cgmQualityInformationPresent) {
                quality = bytes.getFloatValue(FloatFormat.FORMAT_SFLOAT, offset)
                offset += 2
            }

            // E2E-CRC
            if (crcPresent) {
                offset += 2
            }
            CGMRecord(
                glucoseConcentration = glucoseConcentration,
                trend = trend,
                quality = quality,
                status = status,
                timeOffset = timeOffset,
                crcPresent = crcPresent
            ).let { result.add(it) }
        }
        return result.toList()
    }
}
