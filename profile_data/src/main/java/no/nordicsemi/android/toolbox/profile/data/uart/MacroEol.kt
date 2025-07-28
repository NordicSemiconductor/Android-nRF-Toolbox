package no.nordicsemi.android.toolbox.profile.data.uart

enum class MacroEol {
    LF,
    CR,
    CR_LF;
}

fun String.parseWithNewLineChar(newLineChar: MacroEol): String {
    return when (newLineChar) {
        MacroEol.LF -> this
        MacroEol.CR_LF -> this.replace("\n", "\r\n")
        MacroEol.CR -> this.replace("\n", "\r")
    }
}
