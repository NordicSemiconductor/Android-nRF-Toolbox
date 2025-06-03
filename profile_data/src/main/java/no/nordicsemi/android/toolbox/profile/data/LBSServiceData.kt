package no.nordicsemi.android.toolbox.profile.data

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