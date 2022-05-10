package no.nordicsemi.android.uart.data

internal data class UARTData(
    val messages: List<UARTRecord> = emptyList(),
    val batteryLevel: Int? = null,
) {

    val displayMessages = messages
}

internal data class UARTRecord(
    val text: String,
    val type: UARTRecordType,
    val timestamp: Long = System.currentTimeMillis()
)

enum class UARTRecordType {
    INPUT, OUTPUT
}
