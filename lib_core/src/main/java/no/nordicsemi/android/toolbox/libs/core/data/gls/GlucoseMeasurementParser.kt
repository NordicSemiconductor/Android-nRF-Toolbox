package no.nordicsemi.android.toolbox.libs.core.data.gls

import no.nordicsemi.android.toolbox.libs.core.data.gls.data.ConcentrationUnit
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSRecord
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GlucoseStatus
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RecordType
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.SampleLocation
import no.nordicsemi.android.toolbox.libs.core.data.hts.DateTimeParser
import no.nordicsemi.kotlin.data.getFloat
import no.nordicsemi.kotlin.data.getInt
import java.nio.ByteOrder
import java.util.Calendar

object GlucoseMeasurementParser {

    fun parse(data: ByteArray, byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN): GLSRecord? {
        if (data.size < 10) return null

        var offset = 0

        val flags: Int = data.getInt(offset++, no.nordicsemi.kotlin.data.IntFormat.UINT8)
        val timeOffsetPresent = flags and 0x01 != 0
        val glucoseDataPresent = flags and 0x02 != 0
        val unitMolL = flags and 0x04 != 0
        val sensorStatusAnnunciationPresent = flags and 0x08 != 0
        val contextInformationFollows = flags and 0x10 != 0

        if (data.size < (10 + (if (timeOffsetPresent) 2 else 0) + (if (glucoseDataPresent) 3 else 0)
                    + if (sensorStatusAnnunciationPresent) 2 else 0)
        ) {
            return null
        }

        // Required fields
        val sequenceNumber: Int =
            data.getInt(offset, no.nordicsemi.kotlin.data.IntFormat.UINT16, byteOrder)
        offset += 2
        val baseTime: Calendar = DateTimeParser.parse(data, 3) ?: return null
        offset += 7

        // Optional fields
        if (timeOffsetPresent) {
            val timeOffset: Int =
                data.getInt(offset, no.nordicsemi.kotlin.data.IntFormat.INT16, byteOrder)
            offset += 2
            baseTime.add(Calendar.MINUTE, timeOffset)
        }

        var glucoseConcentration: Float? = null
        var unit: ConcentrationUnit? = null
        var type: Int? = null
        var sampleLocation: Int? = null
        if (glucoseDataPresent) {
            glucoseConcentration = data.getFloat(
                offset,
                no.nordicsemi.kotlin.data.FloatFormat.IEEE_11073_16_BIT,
                byteOrder
            )
            val typeAndSampleLocation: Int =
                data.getInt(offset + 2, no.nordicsemi.kotlin.data.IntFormat.UINT8)
            offset += 3
            type = typeAndSampleLocation and 0x0F
            sampleLocation = typeAndSampleLocation shr 4
            unit = if (unitMolL) ConcentrationUnit.UNIT_MOLPL else ConcentrationUnit.UNIT_KGPL
        }

        var status: GlucoseStatus? = null
        if (sensorStatusAnnunciationPresent) {
            val value: Int =
                data.getInt(offset, no.nordicsemi.kotlin.data.IntFormat.UINT16, byteOrder)
            // offset += 2;
            status = GlucoseStatus(value)
        }

        return GLSRecord(
            sequenceNumber,
            baseTime /* with offset */,
            glucoseConcentration,
            unit,
            RecordType.createOrNull(type),
            status,
            SampleLocation.createOrNull(sampleLocation),
            contextInformationFollows
        )
    }
}