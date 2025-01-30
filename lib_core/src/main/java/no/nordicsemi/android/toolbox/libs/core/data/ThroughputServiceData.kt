package no.nordicsemi.android.toolbox.libs.core.data

import no.nordicsemi.android.lib.profile.throughput.ThroughputMetrics
import no.nordicsemi.android.toolbox.libs.core.Profile

data class ThroughputServiceData(
    override val profile: Profile = Profile.THROUGHPUT,
    val throughputData: ThroughputMetrics? = null,
    val writingStatus: WritingStatus = WritingStatus.IDEAL,
    val maxWriteValueLength: Int? = null
) : ProfileServiceData()


enum class WriteDataType {
    TEXT, HEX, ASCII,
}

enum class WritingStatus {
    IDEAL, IN_PROGRESS, COMPLETED
}