package no.nordicsemi.android.toolbox.libs.core.data

import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.lib.profile.throughput.ThroughputMetrics

data class ThroughputServiceData(
    override val profile: Profile = Profile.THROUGHPUT,
    val throughputData: ThroughputMetrics? = null,
    val isHighestMtuRequested: Boolean = false,
) : ProfileServiceData()


enum class WriteDataType {
    TEXT, HEX
}