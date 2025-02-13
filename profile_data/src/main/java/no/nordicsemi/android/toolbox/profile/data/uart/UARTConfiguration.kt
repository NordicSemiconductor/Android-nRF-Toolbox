package no.nordicsemi.android.toolbox.profile.data.uart

private const val MACROS_SIZES = 9

data class UARTConfiguration(
    val id: Int?,
    val name: String,
    val macros: List<UARTMacro?> = List<UARTMacro?>(9) { null }
) {

    init {
        if (macros.size < 9) {
            throw IllegalArgumentException("Macros should always have 9 positions.")
        }
    }
}