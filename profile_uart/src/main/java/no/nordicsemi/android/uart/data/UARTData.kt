package no.nordicsemi.android.uart.data

internal data class UARTData(
    val messages: List<UARTOutputRecord> = emptyList(),
    val batteryLevel: Int? = null,
) {

    val displayMessages = messages.reversed().take(10)
}

internal data class UARTOutputRecord(
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
