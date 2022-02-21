package no.nordicsemi.android.uart.data

fun String.parseWithNewLineChar(newLineChar: MacroEol): String {
    return when (newLineChar) {
        MacroEol.LF -> this
        MacroEol.CR_LF -> this.replace("\n", "\r\n")
        MacroEol.CR -> this.replace("\n", "\r")
    }
}
