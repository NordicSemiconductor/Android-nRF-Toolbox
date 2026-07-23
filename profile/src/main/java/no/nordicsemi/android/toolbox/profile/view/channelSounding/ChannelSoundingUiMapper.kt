package no.nordicsemi.android.toolbox.profile.view.channelSounding

import androidx.annotation.StringRes
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.RangingSessionFailedReason
import no.nordicsemi.android.toolbox.profile.data.RangingTechnology
import no.nordicsemi.android.toolbox.profile.data.SessionCloseReasonProvider
import no.nordicsemi.android.toolbox.profile.data.SessionClosedReason
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

@StringRes
internal fun SessionCloseReasonProvider.toUiString(): Int {
    return when (this) {
        SessionClosedReason.MISSING_PERMISSION -> R.string.cs_missing_permissions
        SessionClosedReason.NOT_SUPPORTED -> R.string.channel_sounding_not_supported
        SessionClosedReason.RANGING_NOT_AVAILABLE -> R.string.cs_ranging_not_available
        SessionClosedReason.CS_SECURITY_NOT_AVAILABLE -> R.string.cs_security_not_available
        SessionClosedReason.UNKNOWN -> R.string.cs_error_unknown
        RangingSessionFailedReason.LOCAL_REQUEST -> R.string.cs_local_request
        RangingSessionFailedReason.REMOTE_REQUEST -> R.string.cs_remote_request
        RangingSessionFailedReason.UNSUPPORTED -> R.string.cs_params_unsupported
        RangingSessionFailedReason.SYSTEM_POLICY -> R.string.cs_system_policy
        RangingSessionFailedReason.NO_PEERS_FOUND -> R.string.cs_no_peers_found
        RangingSessionFailedReason.UNKNOWN -> R.string.cs_error_unknown
    }
}

