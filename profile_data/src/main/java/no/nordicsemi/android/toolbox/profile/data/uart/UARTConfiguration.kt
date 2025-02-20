package no.nordicsemi.android.toolbox.profile.data.uart

private const val MACROS_SIZES = 8

data class UARTConfiguration(
    val id: Int?,
    val name: String,
    val macros: List<UARTMacro?> = List<UARTMacro?>(MACROS_SIZES) { null }
) {

    init {
        if (macros.size < MACROS_SIZES) {
            throw IllegalArgumentException("Macros should always have 8 positions.")
        }
    }
}