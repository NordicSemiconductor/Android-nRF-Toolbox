package no.nordicsemi.android.toolbox.libs.core.data

import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.toolbox.libs.core.data.rscs.RSCSData
import no.nordicsemi.android.toolbox.libs.core.data.rscs.RSCSSettingsUnit

data class RSCSServiceData(
    override val profile: Profile = Profile.RSCS,
    val data: RSCSData = RSCSData(),
    val unit: RSCSSettingsUnit? = RSCSSettingsUnit.UNIT_M,
) : ProfileServiceData()
