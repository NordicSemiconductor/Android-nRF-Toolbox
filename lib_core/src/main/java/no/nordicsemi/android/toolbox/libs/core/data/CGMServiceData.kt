package no.nordicsemi.android.toolbox.libs.core.data

import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.lib.profile.cgms.data.CGMRecord
import no.nordicsemi.android.lib.profile.common.WorkingMode
import no.nordicsemi.android.lib.profile.gls.data.RequestStatus

data class CGMServiceData(
    override val profile: Profile = Profile.CGM,
    val records: List<CGMRecordWithSequenceNumber> = emptyList(),
    val requestStatus: RequestStatus = RequestStatus.IDLE,
    val workingMode: WorkingMode? = null,
) : ProfileServiceData()

data class CGMRecordWithSequenceNumber(
    val sequenceNumber: Int,
    val record: CGMRecord,
    val timestamp: Long
)
