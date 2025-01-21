package no.nordicsemi.android.toolbox.libs.core.data.csc

internal data class CSCDataSnapshot(
    val wheelRevolutions: Long? = null,
    val wheelEventTime: Int? = null,
    val crankRevolutions: Long? = null,
    val crankEventTime: Int? = null,
)