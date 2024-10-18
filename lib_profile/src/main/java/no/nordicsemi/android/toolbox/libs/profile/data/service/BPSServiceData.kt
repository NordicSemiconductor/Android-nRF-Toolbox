package no.nordicsemi.android.toolbox.libs.profile.data.service

import no.nordicsemi.android.toolbox.libs.profile.data.Profile
import no.nordicsemi.android.toolbox.libs.profile.data.bps.BloodPressureMeasurementData
import no.nordicsemi.android.toolbox.libs.profile.data.bps.IntermediateCuffPressureData

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
