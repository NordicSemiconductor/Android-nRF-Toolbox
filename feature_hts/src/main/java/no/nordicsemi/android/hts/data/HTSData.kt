package no.nordicsemi.android.hts.data

internal data class HTSData(
    val heartRates: List<Int> = emptyList(),
    val temperature: Temperature = Temperature.CELSIUS,
    val batteryLevel: Int = 0,
    val sensorLocation: Int = 0,
    val isScreenActive: Boolean = true
) {

    fun displayTemperature() {
        val value = when (temperature) {
            Temperature.CELSIUS -> TODO()
            Temperature.FAHRENHEIT -> TODO()
            Temperature.KELVIN -> TODO()
        }
    }
}

internal enum class Temperature {
    CELSIUS, FAHRENHEIT, KELVIN
}
