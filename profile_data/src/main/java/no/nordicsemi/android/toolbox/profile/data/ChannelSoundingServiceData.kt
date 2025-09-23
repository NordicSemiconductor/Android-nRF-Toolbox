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
    data class OnResult(
        val data: RangingData,
        val previousData: List<Float> = emptyList()
    ) : RangingSessionAction

    data class OnError(val reason: String) : RangingSessionAction
    object OnClosed : RangingSessionAction
}

enum class UpdateRate {
    NORMAL,
    FREQUENT,
    INFREQUENT;
}

enum class ConfidenceLevel(val value: Int) {
    CONFIDENCE_HIGH(2),
    CONFIDENCE_MEDIUM(1),
    CONFIDENCE_LOW(0);

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

    companion object {
        fun from(value: Int): RangingTechnology? = entries.find { it.value == value }
    }
}
