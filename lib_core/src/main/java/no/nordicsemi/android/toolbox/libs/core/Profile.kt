package no.nordicsemi.android.toolbox.libs.core

enum class Profile {
    BPS,
    CGM,
    CSC,
    GLS,
    HRS,
    HTS,
    RSCS,
    PRX,
    BATTERY,
    UART;

    override fun toString(): String = this.name

}