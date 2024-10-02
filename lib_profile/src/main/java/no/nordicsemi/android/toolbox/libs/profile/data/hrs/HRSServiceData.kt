package no.nordicsemi.android.toolbox.libs.profile.data.hrs
/**
 * Heart Rate data.
 * @param heartRate the heart rate value.
 * @param sensorContact true if the sensor contact is supported.
 * @param energyExpanded the energy expanded in joules.
 * @param rrIntervals the RR intervals in milliseconds.
 */
data class HRSData(
    val heartRate: Int,
    val sensorContact: Boolean,
    val energyExpanded: Int?,
    val rrIntervals: List<Int>
)

/**
 * Heart Rate Service data.
 * @param data the list of heart rate data.
 * @param bodySensorLocation the body sensor location.
 * @param zoomIn true if the chart is zoomed in.
 */
data class HRSServiceData(
    val data: List<HRSData> = emptyList(),
    val bodySensorLocation: Int? = null,
    val zoomIn: Boolean = false,
) {
    val heartRates = data.map { it.heartRate }
}
