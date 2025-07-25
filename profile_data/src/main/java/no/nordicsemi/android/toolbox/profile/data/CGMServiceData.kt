package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.toolbox.profile.parser.cgms.data.CGMRecord
import no.nordicsemi.android.toolbox.profile.parser.common.WorkingMode
import no.nordicsemi.android.toolbox.profile.parser.gls.data.RequestStatus
import no.nordicsemi.android.toolbox.lib.utils.Profile

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
