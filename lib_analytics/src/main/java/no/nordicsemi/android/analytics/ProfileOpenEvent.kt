package no.nordicsemi.android.analytics

enum class ProfileOpenEvent(internal val firebaseName: String) {
    BPS("BPS_PROFILE_OPEN"),
    CGMS("CGMS_PROFILE_OPEN"),
    CSC("CSC_PROFILE_OPEN"),
    GLS("GLS_PROFILE_OPEN"),
    HRS("HRS_PROFILE_OPEN"),
    HTS("HTS_PROFILE_OPEN"),
    PRX("PRX_PROFILE_OPEN"),
    RSCS("RSCS_PROFILE_OPEN"),
    UART("UART_PROFILE_OPEN"),

    DFU("DFU_PROFILE_OPEN"),
    LOGGER("LOGGER_PROFILE_OPEN"),
}
