package no.nordicsemi.android.uart.data

fun NewLineChar.parseString(text: String): String {
    return when (this) {
        NewLineChar.LF -> text
        NewLineChar.CR_LF -> text.replace("\n", "\r\n")
        NewLineChar.CR -> text.replace("\n", "\r")
    }
}
