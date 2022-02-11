package no.nordicsemi.android.csc.data

internal data class CSCData(
    val scanDevices: Boolean = false,
    val speed: Float = 0f,
    val cadence: Float = 0f,
    val distance: Float = 0f,
    val totalDistance: Float = 0f,
    val gearRatio: Float = 0f,
    val batteryLevel: Int = 0,
    val wheelSize: WheelSize = WheelSize()
)
