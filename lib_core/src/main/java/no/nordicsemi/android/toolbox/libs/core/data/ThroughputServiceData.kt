package no.nordicsemi.android.toolbox.libs.core.data

import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.lib.profile.throughput.ThroughputMetrics

data class ThroughputServiceData(
    override val profile: Profile = Profile.THROUGHPUT,
    val throughputData: ThroughputMetrics? = null,
    val isHighestMtuRequested: Boolean = false,
    val writingStatus: WritingStatus = WritingStatus.IDEAL,
) : ProfileServiceData()


enum class WriteDataType {
    TEXT, HEX, ASCII,
}

enum class WritingStatus {
    IDEAL, IN_PROGRESS, COMPLETED
}