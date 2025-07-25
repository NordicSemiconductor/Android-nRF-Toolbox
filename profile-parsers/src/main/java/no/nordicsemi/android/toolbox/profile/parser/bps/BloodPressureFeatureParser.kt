package no.nordicsemi.android.toolbox.profile.parser.bps

/**
 * Blood Pressure Feature data parser.
 * This parser is used to parse the Blood Pressure Feature data from the Bluetooth GATT characteristic.
 * The data is a 2-byte value that contains flags indicating the supported features of the Blood Pressure service.
 */
object BloodPressureFeatureParser {

    fun parse(data: ByteArray): BloodPressureFeatureData? {
        if (data.size != 2) return null
        val flags: Int = data[0].toInt() or (data[1].toInt() shl 8)
        val bodyMovementDetection = flags and 0x0001 != 0
        val cuffFitDetection = flags and 0x0002 != 0
        val irregularPulseDetection = flags and 0x0004 != 0
        val pulseRateRangeDetection = flags and 0x0008 != 0
        val measurementPositionDetection = flags and 0x0010 != 0

        return BloodPressureFeatureData(
            bodyMovementDetection = bodyMovementDetection,
            cuffFitDetection = cuffFitDetection,
            irregularPulseDetection = irregularPulseDetection,
            pulseRateRangeDetection = pulseRateRangeDetection,
            measurementPositionDetection = measurementPositionDetection
        )
    }
}
