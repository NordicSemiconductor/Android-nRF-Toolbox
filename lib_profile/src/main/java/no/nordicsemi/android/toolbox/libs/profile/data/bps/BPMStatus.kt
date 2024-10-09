package no.nordicsemi.android.toolbox.libs.profile.data.bps

class BPMStatus(
    val bodyMovementDetected: Boolean,
    val cuffTooLose: Boolean,
    val irregularPulseDetected: Boolean,
    val pulseRateInRange: Boolean,
    val pulseRateExceedsUpperLimit: Boolean,
    val pulseRateIsLessThenLowerLimit: Boolean,
    val improperMeasurementPosition: Boolean
) {
    constructor(value: Int) : this(
        bodyMovementDetected = value and 0x01 != 0,
        cuffTooLose = value and 0x02 != 0,
        irregularPulseDetected = value and 0x04 != 0,
        pulseRateInRange = value and 0x18 shr 3 == 0,
        pulseRateExceedsUpperLimit = value and 0x18 shr 3 == 1,
        pulseRateIsLessThenLowerLimit = value and 0x18 shr 3 == 2,
        improperMeasurementPosition = value and 0x20 != 0
    )
}