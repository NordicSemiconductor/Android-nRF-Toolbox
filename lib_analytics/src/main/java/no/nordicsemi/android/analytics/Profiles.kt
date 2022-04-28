package no.nordicsemi.android.analytics

enum class Profile(val displayName: String) {
    BPS("BPS"),
    CGMS("CGMS"),
    CSC("CSC"),
    GLS("GLS"),
    HRS("HRS"),
    HTS("HTS"),
    PRX("PRX"),
    RSCS("RSCS"),
    UART("UART");
}

enum class Link(val displayName: String) {
    DFU("DFU"),
    LOGGER("LOGGER");
}
