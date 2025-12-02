package no.nordicsemi.android.toolbox.profile.data.directionFinder

import no.nordicsemi.android.toolbox.profile.data.SensorData
import no.nordicsemi.android.toolbox.profile.data.SensorValue

fun <T, R : Comparable<R>> SensorValue<T>?.medianValue(selector: (T) -> R): R? =
    this?.values?.map(selector)?.sorted()?.let { it.getOrNull(it.size / 2) }

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

fun SensorData.isAzimuthAndElevationDataAvailable() = azimuthValue() != null && elevationValue() != null

fun SensorData.isMcpdSectionAvailable() =
    ifftValue() != null || rssiValue() != null || phaseSlopeValue() != null || bestEffortValue() != null