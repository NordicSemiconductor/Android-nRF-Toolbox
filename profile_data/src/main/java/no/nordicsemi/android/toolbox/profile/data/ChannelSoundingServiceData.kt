package no.nordicsemi.android.toolbox.profile.data

import android.ranging.RangingData
import no.nordicsemi.android.toolbox.lib.utils.Profile

data class ChannelSoundingServiceData(
    override val profile: Profile = Profile.CHANNEL_SOUNDING,
    val rangingSessionAction: RangingSessionAction? = null,
    val updateRate: UpdateRate = UpdateRate.NORMAL,
    val interval: Int = 1000,
) : ProfileServiceData()

sealed interface RangingSessionAction {
    object OnStart : RangingSessionAction
    data class OnResult(val data: RangingData) : RangingSessionAction
    data class OnError(val reason: String) : RangingSessionAction
    object OnClosed : RangingSessionAction
}

enum class UpdateRate {
    FREQUENT,
    INFREQUENT,
    NORMAL;

    override fun toString(): String {
        return when (this) {
            FREQUENT -> "Frequent"
            INFREQUENT -> "Infrequent"
            NORMAL -> "Normal"
        }
    }

    companion object {
        fun description(value: UpdateRate): String {
            return when (value) {
                FREQUENT -> "Updates every 100 milliseconds."
                INFREQUENT -> "Updates every 5 seconds."
                NORMAL -> "Updates every 200 milliseconds."
            }
        }
    }
}

enum class ConfidenceLevel(val value: Int) {
    CONFIDENCE_HIGH(2),
    CONFIDENCE_MEDIUM(1),
    CONFIDENCE_LOW(0);

    override fun toString(): String {
        return when (this) {
            CONFIDENCE_HIGH -> "High"
            CONFIDENCE_MEDIUM -> "Medium"
            CONFIDENCE_LOW -> "Low"
        }
    }

    companion object {
        fun from(value: Int): ConfidenceLevel? = entries.find { it.value == value }
    }
}

enum class RangingTechnology(val value: Int) {
    BLE_CS(1),
    BLE_RSSI(3),
    UWB(0),
    WIFI_NAN_RTT(2),
    WIFI_STA_RTT(4), ;

    override fun toString(): String {
        return when (this) {
            BLE_CS -> "Bluetooth LE Channel Sounding"
            BLE_RSSI -> "Bluetooth LE RSSI"
            UWB -> "UWB"
            WIFI_NAN_RTT -> "Wifi NAN RTT"
            WIFI_STA_RTT -> "Wifi STA RTT"
        }
    }

    companion object {
        fun from(value: Int): RangingTechnology? = entries.find { it.value == value }

        fun displayString(value: Int): String {
            return from(value)?.toString() ?: "Unknown"
        }
    }
}
