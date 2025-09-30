package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.toolbox.lib.utils.Profile

data class DFUServiceData(
    override val profile: Profile = Profile.DFU,
    val dfuAppName : String? = null,
): ProfileServiceData()