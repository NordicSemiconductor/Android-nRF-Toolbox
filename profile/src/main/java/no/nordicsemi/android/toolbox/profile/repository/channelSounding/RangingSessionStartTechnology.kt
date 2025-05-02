package no.nordicsemi.android.toolbox.profile.repository.channelSounding

enum class RangingSessionStartTechnology(val technology: Int) {
    UWB(0),
    BLE_CS(1),
    WIFI_NAN_RTT(2),
    BLE_RSSI(3), ;

    override fun toString(): String {
        return when (technology) {
            UWB.technology -> "UWB"
            BLE_CS.technology -> "BLE CS"
            WIFI_NAN_RTT.technology -> "WIFI NAN RTT"
            BLE_RSSI.technology -> "BLE RSSI"
            else -> "Unknown technology"
        }
    }

    companion object {
        fun getTechnology(technology: Int): String {
            return entries.firstOrNull { it.technology == technology }.toString()
        }
    }
}