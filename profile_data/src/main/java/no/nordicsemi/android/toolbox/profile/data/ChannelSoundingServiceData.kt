package no.nordicsemi.android.toolbox.profile.data

import android.ranging.RangingData
import no.nordicsemi.android.toolbox.lib.utils.Profile

data class ChannelSoundingServiceData(
    override val profile: Profile = Profile.CHANNEL_SOUNDING,
    val rangingData: RangingData? = null
) : ProfileServiceData()