package no.nordicsemi.android.toolbox.profile.data

data class LBSServiceData(
    override val profile: Profile = Profile.HTS,
    val data: LBSData? = null,
) : ProfileServiceData()

data class LBSData(
    val ledState: Boolean,
    val buttonState: Boolean,
)