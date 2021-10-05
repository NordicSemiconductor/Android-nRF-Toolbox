package no.nordicsemi.android.hts.data

import no.nordicsemi.android.theme.view.RadioGroupItem

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

    fun temperatureSettingsItems(): List<RadioGroupItem<TemperatureUnit>> {
        return listOf(
            RadioGroupItem(TemperatureUnit.CELSIUS,"°C"),
            RadioGroupItem(TemperatureUnit.FAHRENHEIT, "°F"),
            RadioGroupItem(TemperatureUnit.KELVIN, "°K")
        )
    }
}

internal enum class TemperatureUnit {
    CELSIUS, FAHRENHEIT, KELVIN
}
