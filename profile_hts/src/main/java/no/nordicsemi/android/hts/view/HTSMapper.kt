package no.nordicsemi.android.hts.view

import no.nordicsemi.android.material.you.RadioButtonItem
import no.nordicsemi.android.material.you.RadioGroupViewEntity

private const val DISPLAY_FAHRENHEIT = "°F"
private const val DISPLAY_CELSIUS = "°C"
private const val DISPLAY_KELVIN = "°K"

internal fun displayTemperature(value: Float, temperatureUnit: TemperatureUnit): String {
    return when (temperatureUnit) {
        TemperatureUnit.CELSIUS -> String.format("%.1f °C", value)
        TemperatureUnit.FAHRENHEIT -> String.format("%.1f °F", value * 1.8f + 32f)
        TemperatureUnit.KELVIN -> String.format("%.1f °K", value + 273.15f)
    }
}

internal fun String.toTemperatureUnit(): TemperatureUnit {
    return when (this) {
        DISPLAY_CELSIUS -> TemperatureUnit.CELSIUS
        DISPLAY_FAHRENHEIT -> TemperatureUnit.FAHRENHEIT
        DISPLAY_KELVIN -> TemperatureUnit.KELVIN
        else -> throw IllegalArgumentException("Can't create TemperatureUnit from this label: $this")
    }
}

internal fun TemperatureUnit.temperatureSettingsItems(): RadioGroupViewEntity {
    return RadioGroupViewEntity(
        TemperatureUnit.values().map { createRadioButtonItem(it, this) }
    )
}

private fun createRadioButtonItem(unit: TemperatureUnit, selectedTemperatureUnit: TemperatureUnit): RadioButtonItem {
    return RadioButtonItem(displayTemperature(unit), unit == selectedTemperatureUnit)
}

private fun displayTemperature(unit: TemperatureUnit): String {
    return when (unit) {
        TemperatureUnit.CELSIUS -> DISPLAY_CELSIUS
        TemperatureUnit.FAHRENHEIT -> DISPLAY_FAHRENHEIT
        TemperatureUnit.KELVIN -> DISPLAY_KELVIN
    }
}
