package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.toolbox.lib.utils.Profile

data class ChannelSoundingServiceData(
    override val profile: Profile = Profile.CHANNEL_SOUNDING
) : ProfileServiceData()