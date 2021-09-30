package no.nordicsemi.android.nrftoolbox

enum class NavDestination(val id: String) {
    HOME("home-screen"),
    CSC("csc-screen"),
    HRS("hrs-screen"),
    GLS("gls-screen"),
    REQUEST_PERMISSION("request-permission"),
    BLUETOOTH_NOT_AVAILABLE("bluetooth-not-available"),
    BLUETOOTH_NOT_ENABLED("bluetooth-not-enabled"),
    DEVICE_NOT_CONNECTED("device-not-connected"),
}
