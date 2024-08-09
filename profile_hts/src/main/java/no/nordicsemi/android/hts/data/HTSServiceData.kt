package no.nordicsemi.android.hts.data

import no.nordicsemi.android.hts.view.TemperatureUnit
import no.nordicsemi.kotlin.ble.core.ConnectionState
import java.util.Calendar

data class HtsData(
    val temperature: Float = 0f,
    val unit: TemperatureUnitData = TemperatureUnitData.CELSIUS,
    val timestamp: Calendar? = null,
    val type: Int? = null
)

enum class TemperatureUnitData(private val value: Int) {
    CELSIUS(0),
    FAHRENHEIT(1);

    companion object {
        fun create(value: Int): TemperatureUnitData? {
            return entries.firstOrNull { it.value == value }
        }
    }
}

internal data class HTSServiceData(
    val data: HtsData = HtsData(),
    val batteryLevel: Int? = null,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val deviceName: String? = null,
    val missingServices: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.Connecting,
)
