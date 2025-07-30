package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.toolbox.lib.utils.Profile

data class LBSServiceData(
    override val profile: Profile = Profile.LBS,
    val data: LBSData = LBSData(
        ledState = false,
        buttonState = false,
    ),
) : ProfileServiceData()

data class LBSData(
    val ledState: Boolean,
    val buttonState: Boolean,
)