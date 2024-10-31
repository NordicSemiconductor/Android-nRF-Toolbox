package no.nordicsemi.android.toolbox.libs.core.data

import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.toolbox.libs.core.data.hts.HtsData
import no.nordicsemi.android.toolbox.libs.core.data.hts.TemperatureUnit

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
