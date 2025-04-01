package no.nordicsemi.android.lib.profile.bps

/**
 * Data class to hold the Blood Pressure Feature data.
 * @param bodyMovementDetection Indicates if body movement detection is supported.
 * @param cuffFitDetection Indicates if cuff fit detection is supported.
 * @param irregularPulseDetection Indicates if irregular pulse detection is supported.
 * @param pulseRateRangeDetection Indicates if pulse rate range detection is supported.
 * @param measurementPositionDetection Indicates if measurement position detection is supported.
 */
data class BloodPressureFeatureData(
    val bodyMovementDetection: Boolean,
    val cuffFitDetection: Boolean,
    val irregularPulseDetection: Boolean,
    val pulseRateRangeDetection: Boolean,
    val measurementPositionDetection: Boolean
)
