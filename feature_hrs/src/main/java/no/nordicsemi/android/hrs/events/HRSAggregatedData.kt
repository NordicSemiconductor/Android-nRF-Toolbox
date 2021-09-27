package no.nordicsemi.android.hrs.events

internal data class HRSAggregatedData(
    val heartRates: List<Int> = emptyList(),
    val batteryLevel: Int = 0,
    val sensorLocation: Int = 0
)
