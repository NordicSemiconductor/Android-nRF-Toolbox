package no.nordicsemi.android.toolbox.libs.profile.data.service

import no.nordicsemi.android.toolbox.libs.profile.data.Profile
import no.nordicsemi.android.toolbox.libs.profile.data.hts.HtsData
import no.nordicsemi.android.toolbox.libs.profile.data.hts.TemperatureUnit

/**
 * HTS service data class that holds the HTS data.
 *
 * @param profile The profile.
 * @param data The HTS data.
 * @param temperatureUnit The temperature unit.
 */
data class HTSServiceData(
    override val profile: Profile = Profile.HTS,
    val data: HtsData = HtsData(),
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
) : ProfileServiceData()
