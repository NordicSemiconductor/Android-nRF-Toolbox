package no.nordicsemi.android.hts.data

import no.nordicsemi.android.material.you.RadioButtonItem
import no.nordicsemi.android.material.you.RadioGroupViewEntity

private const val DISPLAY_FAHRENHEIT = "°F"
private const val DISPLAY_CELSIUS = "°C"
private const val DISPLAY_KELVIN = "°K"

internal data class HTSData(
    val temperatureValue: Float = 0f,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val batteryLevel: Int = 0,
) {

    fun displayTemperature(): String {
        return when (temperatureUnit) {
            TemperatureUnit.CELSIUS -> String.format("%.1f °C", temperatureValue)
            TemperatureUnit.FAHRENHEIT -> String.format("%.1f °F", temperatureValue * 1.8f + 32f)
            TemperatureUnit.KELVIN -> String.format("%.1f °K", temperatureValue + 273.15f)
        }
    }

    fun getTemperatureUnit(label: String): TemperatureUnit {
        return when (label) {
            DISPLAY_CELSIUS -> TemperatureUnit.CELSIUS
            DISPLAY_FAHRENHEIT -> TemperatureUnit.FAHRENHEIT
            DISPLAY_KELVIN -> TemperatureUnit.KELVIN
            else -> throw IllegalArgumentException("Can't create TemperatureUnit from this label: $label")
        }
    }

    fun temperatureSettingsItems(): RadioGroupViewEntity {
        return RadioGroupViewEntity(
            TemperatureUnit.values().map { createRadioButtonItem(it) }
        )
    }

    private fun createRadioButtonItem(unit: TemperatureUnit): RadioButtonItem {
        return RadioButtonItem(displayTemperature(unit), unit == temperatureUnit)
    }

    private fun displayTemperature(unit: TemperatureUnit): String {
        return when (unit) {
            TemperatureUnit.CELSIUS -> DISPLAY_CELSIUS
            TemperatureUnit.FAHRENHEIT -> DISPLAY_FAHRENHEIT
            TemperatureUnit.KELVIN -> DISPLAY_KELVIN
        }
    }
}

internal enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT,
    KELVIN
}
