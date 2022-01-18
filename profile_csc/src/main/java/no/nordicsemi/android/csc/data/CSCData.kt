package no.nordicsemi.android.csc.data

import no.nordicsemi.android.csc.view.SpeedUnit
import no.nordicsemi.android.material.you.RadioButtonItem
import no.nordicsemi.android.material.you.RadioGroupViewEntity
import java.util.*

private const val DISPLAY_M_S = "m/s"
private const val DISPLAY_KM_H = "km/h"
private const val DISPLAY_MPH = "mph"

internal data class CSCData(
    val scanDevices: Boolean = false,
    val selectedSpeedUnit: SpeedUnit = SpeedUnit.M_S,
    val speed: Float = 0f,
    val cadence: Float = 0f,
    val distance: Float = 0f,
    val totalDistance: Float = 0f,
    val gearRatio: Float = 0f,
    val batteryLevel: Int = 0,
    val wheelSize: WheelSize = WheelSize()
) {

    private val speedWithUnit = when (selectedSpeedUnit) {
        SpeedUnit.M_S -> speed
        SpeedUnit.KM_H -> speed * 3.6f
        SpeedUnit.MPH -> speed * 2.2369f
    }

    fun displaySpeed(): String {
        return when (selectedSpeedUnit) {
            SpeedUnit.M_S -> String.format("%.1f m/s", speedWithUnit)
            SpeedUnit.KM_H -> String.format("%.1f km/h", speedWithUnit)
            SpeedUnit.MPH -> String.format("%.1f mph", speedWithUnit)
        }
    }

    fun displayCadence(): String {
        return String.format("%.0f RPM", cadence)
    }

    fun displayDistance(): String {
        return when (selectedSpeedUnit) {
            SpeedUnit.M_S -> String.format("%.0f m", distance)
            SpeedUnit.KM_H -> String.format("%.0f m", distance)
            SpeedUnit.MPH -> String.format("%.0f yd", distance)
        }
    }

    fun displayTotalDistance(): String {
        return when (selectedSpeedUnit) {
            SpeedUnit.M_S -> String.format("%.2f km", distance)
            SpeedUnit.KM_H -> String.format("%.2f km", distance)
            SpeedUnit.MPH -> String.format("%.2f mile", distance)
        }
    }

    fun displayGearRatio(): String {
        return String.format(Locale.US, "%.1f", gearRatio)
    }

    fun getSpeedUnit(label: String): SpeedUnit {
        return when (label) {
            DISPLAY_KM_H -> SpeedUnit.KM_H
            DISPLAY_M_S -> SpeedUnit.M_S
            DISPLAY_MPH -> SpeedUnit.MPH
            else -> throw IllegalArgumentException("Can't create SpeedUnit from this label: $label")
        }
    }

    fun temperatureSettingsItems(): RadioGroupViewEntity {
        return RadioGroupViewEntity(
            SpeedUnit.values().map { createRadioButtonItem(it) }
        )
    }

    private fun createRadioButtonItem(unit: SpeedUnit): RadioButtonItem {
        return RadioButtonItem(displayTemperature(unit), unit == selectedSpeedUnit)
    }

    private fun displayTemperature(unit: SpeedUnit): String {
        return when (unit) {
            SpeedUnit.KM_H -> DISPLAY_KM_H
            SpeedUnit.M_S -> DISPLAY_M_S
            SpeedUnit.MPH -> DISPLAY_MPH
        }
    }
}
