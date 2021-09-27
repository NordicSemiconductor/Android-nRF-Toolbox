package no.nordicsemi.android.hrs.viewmodel

data class HRSViewState(
    val points: List<Int> = listOf(1, 2, 3),
    val batteryLevel: Int = 0,
    val sensorLocation: Int = 0,
    val isScreenActive: Boolean = true
)
