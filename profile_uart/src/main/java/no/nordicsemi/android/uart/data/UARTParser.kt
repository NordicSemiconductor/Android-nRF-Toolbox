package no.nordicsemi.android.uart.data

fun String.parseWithNewLineChar(newLineChar: NewLineChar): String {
    return when (newLineChar) {
        NewLineChar.LF -> this
        NewLineChar.CR_LF -> this.replace("\n", "\r\n")
        NewLineChar.CR -> this.replace("\n", "\r")
    }
}
