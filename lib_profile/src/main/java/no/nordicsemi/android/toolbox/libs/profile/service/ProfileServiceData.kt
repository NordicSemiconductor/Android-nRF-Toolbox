package no.nordicsemi.android.toolbox.libs.profile.service

import no.nordicsemi.android.toolbox.libs.profile.data.Profile
import no.nordicsemi.android.toolbox.libs.profile.data.bps.BloodPressureMeasurementData
import no.nordicsemi.android.toolbox.libs.profile.data.bps.IntermediateCuffPressureData
import no.nordicsemi.android.toolbox.libs.profile.data.hrs.HRSData
import no.nordicsemi.android.toolbox.libs.profile.data.hts.HtsData
import no.nordicsemi.android.toolbox.libs.profile.data.hts.TemperatureUnit

/**
 * Profile service data class that holds the profile and the service data.
 */
sealed class ProfileServiceData {
    abstract val profile: Profile
}

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