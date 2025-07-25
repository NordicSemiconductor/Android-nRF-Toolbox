package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.toolbox.profile.parser.common.WorkingMode
import no.nordicsemi.android.toolbox.profile.parser.gls.data.GLSMeasurementContext
import no.nordicsemi.android.toolbox.profile.parser.gls.data.GLSRecord
import no.nordicsemi.android.toolbox.profile.parser.gls.data.RequestStatus
import no.nordicsemi.android.toolbox.lib.utils.Profile

data class GLSServiceData(
    override val profile: Profile = Profile.GLS,
    val records: Map<GLSRecord, GLSMeasurementContext?> = mapOf(),
    val requestStatus: RequestStatus = RequestStatus.IDLE,
    val workingMode: WorkingMode? = null,
) : ProfileServiceData()
