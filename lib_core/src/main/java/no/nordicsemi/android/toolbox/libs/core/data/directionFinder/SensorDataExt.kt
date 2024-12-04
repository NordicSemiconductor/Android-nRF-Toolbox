package no.nordicsemi.android.toolbox.libs.core.data.directionFinder

import no.nordicsemi.android.toolbox.libs.core.data.SensorData
import no.nordicsemi.android.toolbox.libs.core.data.SensorValue

fun <T, R> SensorValue<T>?.mapValues(selector: (T) -> R): List<R>? =
    this?.values?.map(selector)

fun <T, R : Comparable<R>> SensorValue<T>?.medianValue(selector: (T) -> R): R? =
    this?.values?.map(selector)?.sorted()?.let { it.getOrNull(it.size / 2) }

fun SensorData.azimuthValues() = azimuth.mapValues { it.azimuth }

fun SensorData.elevationValues() = elevation.mapValues { it.elevation }

fun SensorData.ifftValues() = mcpdDistance.mapValues { it.mcpd.ifft }

fun SensorData.phaseSlopeValues() = mcpdDistance.mapValues { it.mcpd.phaseSlope }

fun SensorData.rssiValues() = mcpdDistance.mapValues { it.mcpd.rssi }

fun SensorData.bestEffortValues() = mcpdDistance.mapValues { it.mcpd.best }

fun SensorData.rttValues() = rttDistance.mapValues { it.rtt.value }

fun SensorData.azimuthValue() = azimuth.medianValue { it.azimuth }

fun SensorData.elevationValue() = elevation.medianValue { it.elevation }

fun SensorData.ifftValue() = mcpdDistance.medianValue { it.mcpd.ifft }

fun SensorData.phaseSlopeValue() = mcpdDistance.medianValue { it.mcpd.phaseSlope }

fun SensorData.rssiValue() = mcpdDistance.medianValue { it.mcpd.rssi }

fun SensorData.bestEffortValue() = mcpdDistance.medianValue { it.mcpd.best }

fun SensorData.rttValue() = rttDistance.medianValue { it.rtt.value }

fun SensorData.distanceValue() = bestEffortValue() ?: rttValue()

fun SensorData.displayAzimuth() = azimuthValue()?.let { "$it°" }

fun SensorData.displayDistance() = distanceValue()?.let { "${it}dm" }

fun SensorData.displayElevation() = elevationValue()?.let { "$it°" }

fun SensorData.isDistanceSettingsAvailable() = mcpdDistance != null || rttDistance != null

fun SensorData.isMcpdSectionAvailable() =
    rttValue() != null || rssiValue() != null || phaseSlopeValue() != null || bestEffortValue() != null

enum class MeasurementSection(val displayName: String) {
    DISTANCE_RTT("RTT"),
    DISTANCE_MCPD_IFFT("IFFT"),
    DISTANCE_MCPD_PHASE_SLOPE("Phase"),
    DISTANCE_MCPD_RSSI("Rssi"),
    DISTANCE_MCPD_BEST("Best"),
}

fun SensorData.availableSections(): List<MeasurementSection> = listOfNotNull(
    this.rttValue()?.let { MeasurementSection.DISTANCE_RTT },
    this.rssiValue()?.let { MeasurementSection.DISTANCE_MCPD_RSSI },
    this.ifftValue()?.let { MeasurementSection.DISTANCE_MCPD_IFFT },
    this.phaseSlopeValue()?.let { MeasurementSection.DISTANCE_MCPD_PHASE_SLOPE },
    this.bestEffortValue()?.let { MeasurementSection.DISTANCE_MCPD_BEST },
)

// Direction Finder Profile Events
data class Range(
    val from: Int,
    val to: Int
)

fun SensorData.selectedMeasurementSectionValues(): List<Int>? =
    when (this.selectedMeasurementSection) {
        MeasurementSection.DISTANCE_RTT -> this.rttValues()
        MeasurementSection.DISTANCE_MCPD_IFFT -> this.ifftValues()
        MeasurementSection.DISTANCE_MCPD_PHASE_SLOPE -> this.phaseSlopeValues()
        MeasurementSection.DISTANCE_MCPD_RSSI -> this.rssiValues()
        MeasurementSection.DISTANCE_MCPD_BEST -> this.bestEffortValues()
        null -> null
    }