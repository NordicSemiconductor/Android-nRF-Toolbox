package no.nordicsemi.android.toolbox.lib.utils

enum class Profile {
    BPS,
    CGM,
    CHANNEL_SOUNDING,
    CSC,
    DFS,
    DFU,
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
            DFS -> "Direction Finder Service"
            GLS -> "Glucose"
            HRS -> "Heart Rate Sensor"
            HTS -> "Health Thermometer"
            LBS -> "Blinky/LED Button Service"
            RSCS -> "Running Speed and Cadence Sensor"
            BATTERY -> "Battery Service"
            THROUGHPUT -> "Throughput Service"
            UART -> "UART Service"
            DFU -> "Device Firmware Update"
        }

}