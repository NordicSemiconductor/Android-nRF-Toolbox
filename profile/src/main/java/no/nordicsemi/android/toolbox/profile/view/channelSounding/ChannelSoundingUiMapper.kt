package no.nordicsemi.android.toolbox.profile.view.channelSounding

import androidx.annotation.StringRes
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.AntennaMode
import no.nordicsemi.android.toolbox.profile.data.CapabilityStatus
import no.nordicsemi.android.toolbox.profile.data.LocationType
import no.nordicsemi.android.toolbox.profile.data.RangingSessionFailedReason
import no.nordicsemi.android.toolbox.profile.data.RangingTechnology
import no.nordicsemi.android.toolbox.profile.data.SessionCloseReasonProvider
import no.nordicsemi.android.toolbox.profile.data.SessionClosedReason
import no.nordicsemi.android.toolbox.profile.data.SightType
import no.nordicsemi.android.toolbox.profile.data.UpdateRate
import no.nordicsemi.android.toolbox.profile.data.UpdateRate.FREQUENT
import no.nordicsemi.android.toolbox.profile.data.UpdateRate.INFREQUENT
import no.nordicsemi.android.toolbox.profile.data.UpdateRate.NORMAL

@StringRes
internal fun UpdateRate.toUiString(): Int = when (this) {
    FREQUENT -> R.string.update_rate_frequent
    INFREQUENT -> R.string.update_rate_infrequent
    NORMAL -> R.string.update_rate_normal
}

@StringRes
internal fun SightType.toUiString(): Int = when (this) {
    SightType.UNKNOWN -> R.string.sight_type_unknown
    SightType.LINE_OF_SIGHT -> R.string.sight_type_line_of_sight
    SightType.NON_LINE_OF_SIGHT -> R.string.sight_type_non_line_of_sight
}

@StringRes
internal fun LocationType.toUiString(): Int = when (this) {
    LocationType.UNKNOWN -> R.string.location_type_unknown
    LocationType.INDOOR -> R.string.location_type_indoor
    LocationType.OUTDOOR -> R.string.location_type_outdoor
}

@StringRes
internal fun AntennaMode.toUiString(): Int = when (this) {
    AntennaMode.UNSET -> R.string.antenna_mode_unset
    AntennaMode.OMNI -> R.string.antenna_mode_omni
    AntennaMode.DIRECTIONAL -> R.string.antenna_mode_directional
}

@StringRes
internal fun CapabilityStatus.toUiString(): Int = when (this) {
    CapabilityStatus.ENABLED -> R.string.capability_enabled
    CapabilityStatus.NOT_SUPPORTED -> R.string.capability_not_supported
    CapabilityStatus.DISABLED_USER -> R.string.capability_disabled_user
    CapabilityStatus.DISABLED_REGULATORY -> R.string.capability_disabled_regulatory
    CapabilityStatus.DISABLED_USER_RESTRICTIONS -> R.string.capability_disabled_user_restrictions
    CapabilityStatus.UNKNOWN -> R.string.capability_unknown
}

@StringRes
internal fun RangingTechnology.toUiString(): Int = when (this) {
    RangingTechnology.BLE_CS -> R.string.ranging_tech_ble_cs
    RangingTechnology.BLE_RSSI -> R.string.ranging_tech_ble_rssi
    RangingTechnology.UWB -> R.string.ranging_tech_uwb
    RangingTechnology.WIFI_NAN_RTT -> R.string.ranging_tech_wifi_nan_rtt
    RangingTechnology.WIFI_STA_RTT -> R.string.ranging_tech_wifi_sta_ap_rtt
    RangingTechnology.WIFI_PD -> R.string.ranging_tech_wifi_pd
}

@StringRes
internal fun RangingTechnology.toUiExtraString(): Int? = when (this) {
    RangingTechnology.WIFI_NAN_RTT -> R.string.ranging_tech_wifi_nan_rtt_extra
    RangingTechnology.WIFI_STA_RTT -> R.string.ranging_tech_wifi_sta_ap_rtt_extra
    RangingTechnology.WIFI_PD -> R.string.ranging_tech_wifi_pd_extra
    else -> null
}

@StringRes
internal fun SessionCloseReasonProvider.toUiString(): Int = when (this) {
    SessionClosedReason.MISSING_PERMISSION -> R.string.cs_missing_permissions
    SessionClosedReason.TOO_OLD -> R.string.channel_sounding_not_supported_too_old
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

