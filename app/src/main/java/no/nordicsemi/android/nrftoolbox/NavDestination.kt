package no.nordicsemi.android.nrftoolbox

const val ARGS_KEY = "args"

enum class NavDestination(val id: String) {
    HOME("home-screen"),
    CSC("csc-screen"),
    HRS("hrs-screen"),
    HTS("hts-screen"),
    GLS("gls-screen"),
    BPS("bps-screen"),
    PRX("prx-screen"),
    RSCS("rscs-screen"),
    REQUEST_PERMISSION("request-permission"),
    BLUETOOTH_NOT_AVAILABLE("bluetooth-not-available"),
    BLUETOOTH_NOT_ENABLED("bluetooth-not-enabled"),
    DEVICE_NOT_CONNECTED("device-not-connected/{$ARGS_KEY}");
}
