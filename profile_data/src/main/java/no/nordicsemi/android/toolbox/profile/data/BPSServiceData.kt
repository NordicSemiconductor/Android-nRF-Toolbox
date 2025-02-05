package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.lib.profile.bps.BloodPressureMeasurementData
import no.nordicsemi.android.lib.profile.bps.IntermediateCuffPressureData

/**
 * BPS service data class that holds the blood pressure measurement and intermediate cuff pressure data.
 *
 * @param profile The profile.
 * @param bloodPressureMeasurement The blood pressure measurement data.
 * @param intermediateCuffPressure The intermediate cuff pressure data.
 */
data class BPSServiceData(
    override val profile: Profile = Profile.BPS,
    val bloodPressureMeasurement: BloodPressureMeasurementData? = null,
    val intermediateCuffPressure: IntermediateCuffPressureData? = null,
) : ProfileServiceData()
