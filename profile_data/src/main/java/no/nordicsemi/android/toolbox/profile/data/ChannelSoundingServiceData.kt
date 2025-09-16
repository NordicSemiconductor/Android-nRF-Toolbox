package no.nordicsemi.android.toolbox.profile.data

import android.ranging.RangingData
import no.nordicsemi.android.toolbox.lib.utils.Profile

data class ChannelSoundingServiceData(
    override val profile: Profile = Profile.CHANNEL_SOUNDING,
    val rangingSessionAction: RangingSessionAction? = null,
) : ProfileServiceData()

sealed interface RangingSessionAction {
    object OnStart : RangingSessionAction
    data class OnResult(val data: RangingData) : RangingSessionAction
    data class OnError(val reason: String) : RangingSessionAction
    object OnClosed : RangingSessionAction
}