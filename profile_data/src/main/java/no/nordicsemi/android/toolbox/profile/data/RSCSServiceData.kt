package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.lib.profile.rscs.RSCFeatureData
import no.nordicsemi.android.lib.profile.rscs.RSCSData
import no.nordicsemi.android.lib.profile.rscs.RSCSSettingsUnit
import no.nordicsemi.android.toolbox.lib.utils.Profile

data class RSCSServiceData(
    override val profile: Profile = Profile.RSCS,
    val data: RSCSData = RSCSData(),
    val unit: RSCSSettingsUnit? = RSCSSettingsUnit.UNIT_M,
    val feature: RSCFeatureData? = null,
) : ProfileServiceData()
