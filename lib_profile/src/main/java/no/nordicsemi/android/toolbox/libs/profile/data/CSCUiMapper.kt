package no.nordicsemi.android.toolbox.libs.profile.data

import no.nordicsemi.android.common.ui.view.RadioButtonItem
import no.nordicsemi.android.common.ui.view.RadioGroupViewEntity
import no.nordicsemi.android.toolbox.libs.core.data.csc.CSCData
import no.nordicsemi.android.toolbox.libs.core.data.csc.SpeedUnit
import java.util.Locale

private const val DISPLAY_M_S = "m/s"
private const val DISPLAY_KM_H = "km/h"
private const val DISPLAY_MPH = "mph"

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

internal fun String.toSpeedUnit(): SpeedUnit {
    return when (this) {
        DISPLAY_KM_H -> SpeedUnit.KM_H
        DISPLAY_M_S -> SpeedUnit.M_S
        DISPLAY_MPH -> SpeedUnit.MPH
        else -> throw IllegalArgumentException("Can't create SpeedUnit from this label: $this")
    }
}

internal fun SpeedUnit.temperatureSettingsItems(): RadioGroupViewEntity {
    return RadioGroupViewEntity(
        SpeedUnit.entries.map { createRadioButtonItem(it, this) }
    )
}

private fun createRadioButtonItem(unit: SpeedUnit, selectedSpeedUnit: SpeedUnit): RadioButtonItem {
    return RadioButtonItem(displayTemperature(unit), unit == selectedSpeedUnit)
}

private fun displayTemperature(unit: SpeedUnit): String {
    return when (unit) {
        SpeedUnit.KM_H -> DISPLAY_KM_H
        SpeedUnit.M_S -> DISPLAY_M_S
        SpeedUnit.MPH -> DISPLAY_MPH
    }
}

internal fun Float.toYards(): Float {
    return this * 1.0936f
}

internal fun Float.toKilometers(): Float {
    return this / 1000f
}

internal fun Float.toMiles(): Float {
    return this * 0.0006f
}