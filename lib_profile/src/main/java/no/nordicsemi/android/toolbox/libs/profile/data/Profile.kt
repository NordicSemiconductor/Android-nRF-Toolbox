package no.nordicsemi.android.toolbox.libs.profile.data

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