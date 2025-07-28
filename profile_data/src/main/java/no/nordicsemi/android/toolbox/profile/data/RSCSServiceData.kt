package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.lib.profile.rscs.RSCSData
import no.nordicsemi.android.lib.profile.rscs.RSCSSettingsUnit

data class RSCSServiceData(
    override val profile: Profile = Profile.RSCS,
    val data: RSCSData = RSCSData(),
    val unit: RSCSSettingsUnit? = RSCSSettingsUnit.UNIT_M,
) : ProfileServiceData()
