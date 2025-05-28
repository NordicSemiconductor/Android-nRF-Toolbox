package no.nordicsemi.android.toolbox.profile.data

enum class Profile {
    BPS,
    CGM,
    CHANNEL_SOUNDING,
    CSC,
    DFS,
    GLS,
    HRS,
    HTS,
    LBS,
    RSCS,
    PRX,
    BATTERY,
    THROUGHPUT,
    UART;

    override fun toString(): String = this.name

}