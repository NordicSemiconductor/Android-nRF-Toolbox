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

    override fun toString(): String =
        when (this) {
            BPS -> "Blood Pressure"
            CGM -> "Continuous Glucose Monitoring"
            CHANNEL_SOUNDING -> "Channel Sounding"
            CSC -> "Cycling Speed and Cadence"
            DFS -> "Direction Finding"
            GLS -> "Glucose"
            HRS -> "Heart Rate Sensor"
            HTS -> "Health Thermometer"
            LBS -> "Location and Navigation"
            RSCS -> "Running Speed and Cadence Sensor"
            BATTERY -> "Battery Service"
            THROUGHPUT -> "Throughput Service"
            UART -> "UART Service"
        }

}