package no.nordicsemi.android.toolbox.libs.core.data.csc

internal data class CSCDataSnapshot(
    val wheelRevolutions: Long = -1,
    val wheelEventTime: Int = -1,
    val crankRevolutions: Long = -1,
    val crankEventTime: Int = -1
)