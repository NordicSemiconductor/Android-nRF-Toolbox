package no.nordicsemi.android.toolbox.profile.data

enum class Profile {
    BPS,
    CGM,
    CSC,
    DFS,
    GLS,
    HRS,
    HTS,
    RSCS,
    PRX,
    BATTERY,
    THROUGHPUT,
    UART;

    override fun toString(): String = this.name

}