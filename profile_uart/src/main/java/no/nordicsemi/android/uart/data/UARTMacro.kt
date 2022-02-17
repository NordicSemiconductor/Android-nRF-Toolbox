package no.nordicsemi.android.uart.data

data class UARTMacro(val command: String, val newLineChar: NewLineChar)

enum class NewLineChar {
    LF, CR_LF, CR
}
