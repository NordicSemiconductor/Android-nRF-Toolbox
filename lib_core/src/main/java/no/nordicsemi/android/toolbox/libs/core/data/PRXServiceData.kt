package no.nordicsemi.android.toolbox.libs.core.data

import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.toolbox.libs.core.data.prx.AlarmLevel

/**
 * PRX service data class that holds the PRX data.
 *
 * @param profile The profile.
 * @param localAlarmLevel The local alarm level.
 * @param linkLossAlarmLevel The link loss alarm level.
 * @param isRemoteAlarm True if the remote alarm is set.
 */
data class PRXServiceData(
    override val profile: Profile = Profile.PRX,
    val localAlarmLevel: AlarmLevel = AlarmLevel.NONE,
    val linkLossAlarmLevel: AlarmLevel = AlarmLevel.HIGH,
    val isRemoteAlarm: Boolean = false,
) : ProfileServiceData()
