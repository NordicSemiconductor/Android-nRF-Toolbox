package no.nordicsemi.android.lib.profile.cgms

import no.nordicsemi.android.lib.profile.cgms.data.CGMRecord
import no.nordicsemi.android.lib.profile.cgms.data.CGMStatus
import no.nordicsemi.android.lib.profile.common.CRC16
import no.nordicsemi.kotlin.data.FloatFormat
import no.nordicsemi.kotlin.data.IntFormat
import no.nordicsemi.kotlin.data.getFloat
import no.nordicsemi.kotlin.data.getInt
import java.nio.ByteOrder

object CGMMeasurementParser {

    fun parse(
        data: ByteArray,
        byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN
    ): List<CGMRecord>? {

        if (data.isEmpty()) return null

        var offset = 0

        val result = mutableListOf<CGMRecord>()

        while (offset < data.size) {
            // Packet size
            val size: Int = data.getInt(offset, IntFormat.UINT8)

            if (size < 6 || offset + size > data.size) return null

            // Flags
            val flags: Int = data.getInt(offset + 1, IntFormat.UINT8)

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
                val expectedCrc: Int = data.getInt(offset + dataSize, IntFormat.UINT16, byteOrder)
                val actualCrc: Int = CRC16.MCRF4XX(data, offset, dataSize)
                if (expectedCrc != actualCrc) {
                    continue
                }
            }
            offset += 2

            // Glucose concentration
            val glucoseConcentration: Float = data.getFloat(offset, FloatFormat.IEEE_11073_16_BIT, byteOrder)
            offset += 2

            // Time offset (in minutes since Session Start)
            val timeOffset: Int = data.getInt(offset, IntFormat.UINT16, byteOrder)
            offset += 2

            // Sensor Status Annunciation
            var warningStatus = 0
            var calibrationTempStatus = 0
            var sensorStatus = 0
            var status: CGMStatus? = null

            if (sensorWarningOctetPresent) {
                warningStatus = data.getInt(offset++, IntFormat.UINT8)
            }

            if (sensorCalTempOctetPresent) {
                calibrationTempStatus = data.getInt(offset++, IntFormat.UINT8)
            }

            if (sensorStatusOctetPresent) {
                sensorStatus = data.getInt(offset++, IntFormat.UINT8)
            }

            if (sensorWarningOctetPresent || sensorCalTempOctetPresent || sensorStatusOctetPresent) {
                status = CGMStatus(warningStatus, calibrationTempStatus, sensorStatus)
            }

            // CGM Trend Information
            var trend: Float? = null
            if (cgmTrendInformationPresent) {
                trend = data.getFloat(offset, FloatFormat.IEEE_11073_16_BIT, byteOrder)
                offset += 2
            }

            // CGM Quality Information
            var quality: Float? = null
            if (cgmQualityInformationPresent) {
                quality = data.getFloat(offset, FloatFormat.IEEE_11073_16_BIT, byteOrder)
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
