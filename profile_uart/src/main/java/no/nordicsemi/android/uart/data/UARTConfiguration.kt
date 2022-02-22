package no.nordicsemi.android.uart.data

import no.nordicsemi.android.uart.db.XmlCommand

private const val MACROS_SIZES = 9

data class UARTConfiguration(
    val name: String,
    val macros: List<UARTMacro?> = List<UARTMacro?>(9) { null }
) {

    init {
        if (macros.size < 9) {
            throw IllegalArgumentException("Macros should always have 9 positions.")
        }
    }
}
