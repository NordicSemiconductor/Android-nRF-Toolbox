package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.lib.profile.throughput.ThroughputMetrics

data class ThroughputServiceData(
    override val profile: Profile = Profile.THROUGHPUT,
    val throughputData: ThroughputMetrics? = null,
    val writingStatus: WritingStatus = WritingStatus.IDEAL,
    val maxWriteValueLength: Int? = null
) : ProfileServiceData()

sealed interface ThroughputInputType

data class NumberOfBytes(
    val numberOfBytes: Int
) : ThroughputInputType {

    companion object {
        private const val DISPLAY_NAME = "Number of Kilobytes"
        fun getString(): String = DISPLAY_NAME
    }
}

data class NumberOfSeconds(
    val numberOfSeconds: Int
) : ThroughputInputType {

    companion object {
        private const val DISPLAY_NAME = "Number of seconds"
        fun getString(): String = DISPLAY_NAME
    }
}

enum class WritingStatus {
    IDEAL, IN_PROGRESS, COMPLETED
}