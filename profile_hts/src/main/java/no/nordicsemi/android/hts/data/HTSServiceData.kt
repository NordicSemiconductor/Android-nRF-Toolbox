package no.nordicsemi.android.hts.data

import no.nordicsemi.android.hts.view.TemperatureUnit
import no.nordicsemi.kotlin.ble.core.ConnectionState
import java.util.Calendar

/**
 * HTS data class that holds the temperature data.
 *
 * @param temperature The temperature value.
 * @param unit The unit of the temperature value.
 * @param timestamp The timestamp of the measurement.
 * @param type The type of the measurement.
 */
data class HtsData(
    val temperature: Float = 0f,
    val unit: TemperatureUnitData = TemperatureUnitData.CELSIUS,
    val timestamp: Calendar? = null,
    val type: Int? = null
)

/**
 * The temperature unit data class.
 *
 * @param value The value of the temperature unit.
 */
enum class TemperatureUnitData(private val value: Int) {
    CELSIUS(0),
    FAHRENHEIT(1);

    companion object {
        fun create(value: Int): TemperatureUnitData? {
            return entries.firstOrNull { it.value == value }
        }
    }
}

/**
 * HTS service data class that holds the HTS data.
 *
 * @param data The HTS data.
 * @param batteryLevel The battery level.
 * @param temperatureUnit The temperature unit.
 * @param deviceName The device name.
 * @param missingServices True if the services are missing, false otherwise.
 */
data class HTSServiceData(
    val data: HtsData = HtsData(),
    val batteryLevel: Int? = null,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val deviceName: String? = null,
    val missingServices: Boolean = false,
    val connectionState: ConnectionState? = null,
)
