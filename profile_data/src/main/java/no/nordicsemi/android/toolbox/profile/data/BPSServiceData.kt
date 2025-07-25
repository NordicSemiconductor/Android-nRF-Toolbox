package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.toolbox.profile.parser.bps.BloodPressureFeatureData
import no.nordicsemi.android.toolbox.profile.parser.bps.BloodPressureMeasurementData
import no.nordicsemi.android.toolbox.profile.parser.bps.IntermediateCuffPressureData
import no.nordicsemi.android.toolbox.lib.utils.Profile

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
    val bloodPressureFeature: BloodPressureFeatureData? = null
) : ProfileServiceData()
