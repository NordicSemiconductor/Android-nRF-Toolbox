package no.nordicsemi.android.analytics

sealed interface AppEvent {
    val eventName: String
}

object AppOpenEvent : AppEvent {
    override val eventName: String = "APP_OPEN"
}

enum class ProfileOpenEvent(override val eventName: String) : AppEvent {
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

enum class ProfileConnectedEvent(override val eventName: String) : AppEvent {
    BPS("BPS_CONNECTED"),
    CGMS("CGMS_CONNECTED"),
    CSC("CSC_CONNECTED"),
    GLS("GLS_CONNECTED"),
    HRS("HRS_CONNECTED"),
    HTS("HTS_CONNECTED"),
    PRX("PRX_CONNECTED"),
    RSCS("RSCS_CONNECTED"),
    UART("UART_CONNECTED"),
}
