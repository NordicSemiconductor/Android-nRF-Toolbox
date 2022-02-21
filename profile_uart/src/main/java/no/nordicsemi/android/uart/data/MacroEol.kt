package no.nordicsemi.android.uart.data

enum class MacroEol(val eolIndex: Int) {
    LF(0),
    CR(1),
    CR_LF(2);
}
