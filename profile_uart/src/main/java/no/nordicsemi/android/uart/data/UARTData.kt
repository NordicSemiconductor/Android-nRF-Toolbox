package no.nordicsemi.android.uart.data

import no.nordicsemi.android.utils.EMPTY

internal data class UARTData(
    val text: String = String.EMPTY,
    val macros: List<UARTMacro> = emptyList(),
    val batteryLevel: Int = 0
)
