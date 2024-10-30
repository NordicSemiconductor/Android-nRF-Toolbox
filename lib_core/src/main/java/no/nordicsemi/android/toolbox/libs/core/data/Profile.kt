package no.nordicsemi.android.toolbox.libs.core.data

enum class Profile {
    BPS,
    CSC,
    GLS,
    HRS,
    HTS,
    RSCS,
    PRX,
    CGM,
    BATTERY,
    UART;

    override fun toString(): String = this.name

}