package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.toolbox.lib.utils.Profile

/**
 * Battery Service data class that holds the battery level.
 *
 * @param profile The profile.
 * @param batteryLevel The battery level.
 */
data class BatteryServiceData(
    override val profile: Profile = Profile.BATTERY,
    val batteryLevel: Int? = null,
) : ProfileServiceData()
