package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.lib.profile.throughput.ThroughputMetrics
import no.nordicsemi.android.toolbox.lib.utils.Profile

data class ThroughputServiceData(
    override val profile: Profile = Profile.THROUGHPUT,
    val throughputData: ThroughputMetrics = ThroughputMetrics(),
    val writingStatus: WritingStatus = WritingStatus.IDEAL,
    val maxWriteValueLength: Int? = null
) : ProfileServiceData()

sealed interface ThroughputInputType

data class NumberOfBytes(
    val numberOfBytes: Int
) : ThroughputInputType {

    companion object {
        private const val DISPLAY_NAME = "Test in size (in kB) "
        fun getString(): String = DISPLAY_NAME
    }
}

data class NumberOfSeconds(
    val numberOfSeconds: Int
) : ThroughputInputType {

    companion object {
        private const val DISPLAY_NAME = "Test in time (seconds)"
        fun getString(): String = DISPLAY_NAME
    }
}

enum class WritingStatus {
    IDEAL, IN_PROGRESS, COMPLETED
}