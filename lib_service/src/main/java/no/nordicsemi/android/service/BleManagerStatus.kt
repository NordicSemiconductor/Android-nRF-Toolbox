package no.nordicsemi.android.service

enum class BleManagerStatus {
    CONNECTING, OK, DISCONNECTED
}

enum class BleServiceStatus {
    CONNECTING, OK, DISCONNECTED, LINK_LOSS;

    fun mapToSimpleManagerStatus(): BleManagerStatus {
        return when (this) {
            CONNECTING -> BleManagerStatus.CONNECTING
            OK -> BleManagerStatus.OK
            DISCONNECTED -> BleManagerStatus.DISCONNECTED
            LINK_LOSS -> BleManagerStatus.DISCONNECTED
        }
    }
}
