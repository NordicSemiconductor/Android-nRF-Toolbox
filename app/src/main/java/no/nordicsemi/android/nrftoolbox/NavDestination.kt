package no.nordicsemi.android.nrftoolbox

const val ARGS_KEY = "args"

enum class NavDestination(val id: String, val pairingRequired: Boolean) {
    HOME("home-screen", false),
    CSC("csc-screen", false),
    HRS("hrs-screen", false),
    HTS("hts-screen", false),
    GLS("gls-screen", true),
    BPS("bps-screen", false),
    PRX("prx-screen", true),
    RSCS("rscs-screen", false),
    CGMS("cgms-screen", false),
    REQUEST_PERMISSION("request-permission", false),
    BLUETOOTH_NOT_AVAILABLE("bluetooth-not-available", false),
    BLUETOOTH_NOT_ENABLED("bluetooth-not-enabled", false),
    DEVICE_NOT_CONNECTED("device-not-connected/{$ARGS_KEY}", false),
    BONDING("bonding", false);
}
