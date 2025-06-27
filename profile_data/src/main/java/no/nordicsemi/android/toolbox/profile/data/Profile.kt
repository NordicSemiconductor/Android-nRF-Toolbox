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

    //    PRX, TODO: PRX is not implemented yet, it will be added in the future.
    BATTERY,
    THROUGHPUT,
    UART;

    override fun toString(): String = this.name

}