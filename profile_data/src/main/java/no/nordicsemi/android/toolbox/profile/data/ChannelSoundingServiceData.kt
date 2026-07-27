package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.toolbox.lib.utils.Profile

data class ChannelSoundingServiceData(
    override val profile: Profile = Profile.CHANNEL_SOUNDING,
    val rangingSessionAction: RangingSessionAction? = null,
    val config: RangingOptions = RangingOptions(),
    val capabilities: HostCapabilities? = null,
    val isRunning: Boolean = false,
) : ProfileServiceData()

/**
 * User-selectable parameters for a ranging session. Applied when a session is started; editable
 * only while no session is running.
 */
data class RangingOptions(
    val updateRate: UpdateRate = UpdateRate.NORMAL,
    val sightType: SightType = SightType.UNKNOWN,
    val locationType: LocationType = LocationType.UNKNOWN,
    val sensorFusionEnabled: Boolean = true,
    val antennaMode: AntennaMode = AntennaMode.UNSET,
)

/** Whether there is a direct, unobstructed path between the two ranging devices. */
enum class SightType {
    UNKNOWN,
    LINE_OF_SIGHT,
    NON_LINE_OF_SIGHT;
}

/** The environment the ranging takes place in. */
enum class LocationType {
    UNKNOWN,
    INDOOR,
    OUTDOOR;
}

/** Antenna configuration used by the ranging session (Android 17+). */
enum class AntennaMode {
    UNSET,
    OMNI,
    DIRECTIONAL;
}

/**
 * Snapshot of the host's ranging capabilities, requested once per connection and shown to the user.
 */
data class HostCapabilities(
    val technologies: List<TechnologyAvailability> = emptyList(),
    val channelSoundingSupported: Boolean = false,
    val supportedSecurityLevels: List<Int> = emptyList(),
)

data class TechnologyAvailability(
    val technology: RangingTechnology?,
    val rawValue: Int,
    val status: CapabilityStatus,
)

enum class CapabilityStatus {
    ENABLED,
    NOT_SUPPORTED,
    DISABLED_USER,
    DISABLED_REGULATORY,
    DISABLED_USER_RESTRICTIONS,
    UNKNOWN;
}

sealed interface RangingSessionAction {
    data object OnStart : RangingSessionAction
    data class OnResult(
        val data: CsRangingData,
        val previousData: List<Float> = emptyList()
    ) : RangingSessionAction
    data class OnError(val reason: SessionCloseReasonProvider) : RangingSessionAction
    object OnClosed : RangingSessionAction
    data object OnRestarting : RangingSessionAction
}

data class CsRangingData(
    val distance: CSRangingMeasurement? = null,
    val azimuth: CSRangingMeasurement? = null,
    val elevation: CSRangingMeasurement? = null,
    val technology: RangingTechnology = RangingTechnology.BLE_CS,
    val timeStamp: Long = 0,
    val rssi: Int? = null,
    val distanceStdDevMeters: Double? = null,
    val delaySpreadMeters: Double? = null,
    val velocityMetersPerSec: Double? = null,
    val detectedAttackLevelPercent: Int? = null,
    val remoteTxPowerDbm: Int? = null,
)

data class CSRangingMeasurement(
    val measurement: Double,
    val confidenceLevel: ConfidenceLevel,
)

enum class UpdateRate {
    FREQUENT,
    NORMAL,
    INFREQUENT;
}

enum class ConfidenceLevel(val value: Int) {
    CONFIDENCE_HIGH(2),
    CONFIDENCE_MEDIUM(1),
    CONFIDENCE_LOW(0);

    companion object {
        fun from(value: Int): ConfidenceLevel = entries.find { it.value == value } ?: CONFIDENCE_LOW
    }
}

enum class RangingTechnology(val value: Int) {
    BLE_CS(1),
    BLE_RSSI(3),
    UWB(0),
    WIFI_NAN_RTT(2),
    WIFI_STA_RTT(4),
    WIFI_PD(5);

    companion object {
        fun from(value: Int): RangingTechnology? = entries.find { it.value == value }
    }
}

sealed interface SessionCloseReasonProvider

enum class SessionClosedReason : SessionCloseReasonProvider {
    MISSING_PERMISSION,
    TOO_OLD,
    NOT_SUPPORTED,
    RANGING_NOT_AVAILABLE,
    CS_SECURITY_NOT_AVAILABLE,
    UNKNOWN;
}

enum class RangingSessionFailedReason(val reason: Int) : SessionCloseReasonProvider {
    UNKNOWN(0),
    LOCAL_REQUEST(1),
    REMOTE_REQUEST(2),
    UNSUPPORTED(3),
    SYSTEM_POLICY(4),
    NO_PEERS_FOUND(5), ;

    companion object {
        fun getReason(value: Int): RangingSessionFailedReason {
            return entries.firstOrNull { it.reason == value } ?: UNKNOWN
        }
    }
}
