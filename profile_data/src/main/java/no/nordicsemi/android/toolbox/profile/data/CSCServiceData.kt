package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.lib.profile.csc.CSCData
import no.nordicsemi.android.lib.profile.csc.SpeedUnit
import no.nordicsemi.android.toolbox.lib.utils.Profile

data class CSCServiceData(
    override val profile: Profile = Profile.CSC,
    val data: CSCData = CSCData(),
    val speedUnit: SpeedUnit = SpeedUnit.M_S,
) : ProfileServiceData()
