package no.nordicsemi.android.toolbox.profile.view.channelSounding

import androidx.annotation.StringRes
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.RangingTechnology
import no.nordicsemi.android.toolbox.profile.data.UpdateRate
import no.nordicsemi.android.toolbox.profile.data.UpdateRate.FREQUENT
import no.nordicsemi.android.toolbox.profile.data.UpdateRate.INFREQUENT
import no.nordicsemi.android.toolbox.profile.data.UpdateRate.NORMAL

@StringRes
internal fun UpdateRate.toUiString(): Int {
    return when (this) {
        FREQUENT -> R.string.update_rate_frequent
        INFREQUENT -> R.string.update_rate_infrequent
        NORMAL -> R.string.update_rate_normal
    }
}

@StringRes
internal fun UpdateRate.description(): Int {
    return when (this) {
        FREQUENT -> R.string.update_rate_frequent_des
        INFREQUENT -> R.string.update_rate_infrequent_des
        NORMAL -> R.string.update_rate_normal_des
    }
}

@StringRes
internal fun RangingTechnology.toUiString(): Int {
    return when (this) {
        RangingTechnology.BLE_CS -> R.string.ranging_tech_ble_cs
        RangingTechnology.BLE_RSSI -> R.string.ranging_tech_ble_rssi
        RangingTechnology.UWB -> R.string.ranging_tech_uwb
        RangingTechnology.WIFI_NAN_RTT -> R.string.ranging_tech_wifi_nan_rtt
        RangingTechnology.WIFI_STA_RTT -> R.string.ranging_tech_wifi_sta_rtt
    }

}

