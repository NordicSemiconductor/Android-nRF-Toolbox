package no.nordicsemi.android.toolbox.libs.core.data

import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.toolbox.libs.core.data.cgms.data.CGMRecord
import no.nordicsemi.android.toolbox.libs.core.data.common.WorkingMode
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RequestStatus

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
