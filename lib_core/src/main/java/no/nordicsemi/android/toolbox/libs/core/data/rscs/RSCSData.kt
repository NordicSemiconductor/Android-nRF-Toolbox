package no.nordicsemi.android.toolbox.libs.core.data.rscs

data class RSCSData(
    val running: Boolean = false,
    val instantaneousSpeed: Float = 1.0f,
    val instantaneousCadence: Int = 0,
    val strideLength: Int? = null,
    val totalDistance: Long? = null
)
