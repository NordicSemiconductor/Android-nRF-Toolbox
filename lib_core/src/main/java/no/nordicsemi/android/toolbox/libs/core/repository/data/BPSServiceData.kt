package no.nordicsemi.android.toolbox.libs.core.repository.data

import no.nordicsemi.android.toolbox.libs.core.data.Profile
import no.nordicsemi.android.toolbox.libs.core.data.bps.BloodPressureMeasurementData
import no.nordicsemi.android.toolbox.libs.core.data.bps.IntermediateCuffPressureData

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
