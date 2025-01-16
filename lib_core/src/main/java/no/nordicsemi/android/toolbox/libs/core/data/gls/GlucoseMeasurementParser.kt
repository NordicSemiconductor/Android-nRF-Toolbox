package no.nordicsemi.android.toolbox.libs.core.data.gls

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.FloatFormat
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.ConcentrationUnit
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSRecord
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GlucoseStatus
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RecordType
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.SampleLocation
import no.nordicsemi.android.toolbox.libs.core.data.hts.DateTimeParser
import java.util.Calendar

object GlucoseMeasurementParser {

    fun parse(data: ByteArray): GLSRecord? {
        val bytes = DataByteArray(data)

        if (bytes.size < 10) {
            return null
        }

        var offset = 0

        val flags: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset++) ?: return null
        val timeOffsetPresent = flags and 0x01 != 0
        val glucoseDataPresent = flags and 0x02 != 0
        val unitMolL = flags and 0x04 != 0
        val sensorStatusAnnunciationPresent = flags and 0x08 != 0
        val contextInformationFollows = flags and 0x10 != 0

        if (bytes.size < (10 + (if (timeOffsetPresent) 2 else 0) + (if (glucoseDataPresent) 3 else 0)
                    + if (sensorStatusAnnunciationPresent) 2 else 0)
        ) {
            return null
        }

        // Required fields
        val sequenceNumber: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset) ?: return null
        offset += 2
        val baseTime: Calendar = DateTimeParser.parse(bytes.value, 3) ?: return null
        offset += 7

        // Optional fields
        if (timeOffsetPresent) {
            val timeOffset: Int = bytes.getIntValue(IntFormat.FORMAT_SINT16_LE, offset) ?: return null
            offset += 2
            baseTime.add(Calendar.MINUTE, timeOffset)
        }

        var glucoseConcentration: Float? = null
        var unit: ConcentrationUnit? = null
        var type: Int? = null
        var sampleLocation: Int? = null
        if (glucoseDataPresent) {
            glucoseConcentration = bytes.getFloatValue(FloatFormat.FORMAT_SFLOAT, offset)
            val typeAndSampleLocation: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset + 2) ?: return null
            offset += 3
            type = typeAndSampleLocation and 0x0F
            sampleLocation = typeAndSampleLocation shr 4
            unit = if (unitMolL) ConcentrationUnit.UNIT_MOLPL else ConcentrationUnit.UNIT_KGPL
        }

        var status: GlucoseStatus? = null
        if (sensorStatusAnnunciationPresent) {
            val value: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset) ?: return null
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