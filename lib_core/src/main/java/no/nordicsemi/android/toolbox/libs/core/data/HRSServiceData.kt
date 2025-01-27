package no.nordicsemi.android.toolbox.libs.core.data

import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.lib.profile.hrs.HRSData

/**
 * Heart Rate Service data.
 *
 * @param profile the profile.
 * @param data the list of heart rate data.
 * @param bodySensorLocation the body sensor location.
 * @param zoomIn true if the chart is zoomed in.
 */
data class HRSServiceData(
    override val profile: Profile = Profile.HRS,
    val data: List<HRSData> = emptyList(),
    val bodySensorLocation: Int? = null,
    val zoomIn: Boolean = false,
) : ProfileServiceData() {
    val heartRates = data.map { it.heartRate }
}
