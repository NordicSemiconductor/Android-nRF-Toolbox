package no.nordicsemi.android.toolbox.libs.core.data

import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.toolbox.libs.core.data.rscs.RSCSData

data class RSCSServiceData(
    override val profile: Profile = Profile.RSCS,
    val data: RSCSData = RSCSData(),
) : ProfileServiceData()
