package no.nordicsemi.android.rscs.data

internal data class RSCSData(
    val batteryLevel: Int = 0,
    val running: Boolean = false,
    val instantaneousSpeed: Float = 1.0f,
    val instantaneousCadence: Int = 0,
    val strideLength: Int? = null,
    val totalDistance: Long? = null
) {

    fun displayActivity(): String {
        return if (running) {
            "Running"
        } else {
            "Walking"
        }
    }

    fun displayPace(): String {
        return "$instantaneousCadence min/km"
    }


    fun displayCadence(): String {
        return "$instantaneousCadence RPM"
    }


    fun displayNumberOfSteps(): String {
        if (totalDistance == null || strideLength == null) {
            return "NONE"
        }
        val numberOfSteps = totalDistance/strideLength
        return "Number of Steps $numberOfSteps"
    }
}
