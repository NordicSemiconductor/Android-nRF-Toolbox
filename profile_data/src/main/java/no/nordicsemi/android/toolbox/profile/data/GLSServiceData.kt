package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.lib.profile.common.WorkingMode
import no.nordicsemi.android.lib.profile.gls.data.GLSMeasurementContext
import no.nordicsemi.android.lib.profile.gls.data.GLSRecord
import no.nordicsemi.android.lib.profile.gls.data.RequestStatus

data class GLSServiceData(
    override val profile: Profile = Profile.GLS,
    val records: Map<GLSRecord, GLSMeasurementContext?> = mapOf(),
    val requestStatus: RequestStatus = RequestStatus.IDLE,
    val workingMode: WorkingMode? = null,
) : ProfileServiceData()
