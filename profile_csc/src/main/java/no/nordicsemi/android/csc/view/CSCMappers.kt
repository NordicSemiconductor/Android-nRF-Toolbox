package no.nordicsemi.android.csc.view

import no.nordicsemi.android.csc.data.CSCData
import no.nordicsemi.android.material.you.RadioButtonItem
import no.nordicsemi.android.material.you.RadioGroupViewEntity
import java.util.*

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
        SpeedUnit.M_S -> String.format("%.1f m/s", speedWithUnit)
        SpeedUnit.KM_H -> String.format("%.1f km/h", speedWithUnit)
        SpeedUnit.MPH -> String.format("%.1f mph", speedWithUnit)
    }
}

internal fun CSCData.displayCadence(): String {
    return String.format("%.0f RPM", cadence)
}

internal fun CSCData.displayDistance(speedUnit: SpeedUnit): String {
    return when (speedUnit) {
        SpeedUnit.M_S -> String.format("%.0f m", distance)
        SpeedUnit.KM_H -> String.format("%.0f m", distance)
        SpeedUnit.MPH -> String.format("%.0f yd", distance)
    }
}

internal fun CSCData.displayTotalDistance(speedUnit: SpeedUnit): String {
    return when (speedUnit) {
        SpeedUnit.M_S -> String.format("%.2f km", totalDistance)
        SpeedUnit.KM_H -> String.format("%.2f km", totalDistance)
        SpeedUnit.MPH -> String.format("%.2f mile", totalDistance)
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
        SpeedUnit.values().map { createRadioButtonItem(it, this) }
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

