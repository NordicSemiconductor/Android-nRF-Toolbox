package no.nordicsemi.android.toolbox.profile.view.cscs

import no.nordicsemi.android.lib.profile.csc.CSCData
import no.nordicsemi.android.lib.profile.csc.SpeedUnit
import java.util.Locale

internal fun CSCData.speedWithSpeedUnit(speedUnit: SpeedUnit): Float {
    return when (speedUnit) {
        SpeedUnit.M_S -> speed
        SpeedUnit.KM_H -> speed * 3.6f
        SpeedUnit.MPH -> speed * 2.2369f
    }
}

internal fun CSCData.displaySpeed(speedUnit: SpeedUnit): String {
    val speedWithUnit = speedWithSpeedUnit(speedUnit)
    return when (speedUnit) {
        SpeedUnit.M_S -> String.format(Locale.US, "%.1f m/s", speedWithUnit)
        SpeedUnit.KM_H -> String.format(Locale.US, "%.1f km/h", speedWithUnit)
        SpeedUnit.MPH -> String.format(Locale.US, "%.1f mph", speedWithUnit)
    }
}

internal fun CSCData.displayCadence(): String {
    return String.format(Locale.US, "%.0f RPM", cadence)
}

internal fun CSCData.displayDistance(speedUnit: SpeedUnit): String {
    return when (speedUnit) {
        SpeedUnit.M_S -> String.format(Locale.US, "%.0f m", distance)
        SpeedUnit.KM_H -> String.format(Locale.US, "%.0f m", distance)
        SpeedUnit.MPH -> String.format(Locale.US, "%.0f yd", distance.toYards())
    }
}

internal fun CSCData.displayTotalDistance(speedUnit: SpeedUnit): String {
    return when (speedUnit) {
        SpeedUnit.M_S -> String.format(Locale.US, "%.2f m", totalDistance)
        SpeedUnit.KM_H -> String.format(Locale.US, "%.2f km", totalDistance.toKilometers())
        SpeedUnit.MPH -> String.format(Locale.US, "%.2f mile", totalDistance.toMiles())
    }
}

internal fun CSCData.displayGearRatio(): String {
    return String.format(Locale.US, "%.1f", gearRatio)
}

internal fun Float.toYards(): Float {
    return this * 1.0936f
}

private fun Float.toKilometers(): Float {
    return this / 1000f
}

private fun Float.toMiles(): Float {
    return this * 0.0006f
}