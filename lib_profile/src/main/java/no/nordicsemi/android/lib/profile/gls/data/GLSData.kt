package no.nordicsemi.android.lib.profile.gls.data

internal data class GLSData(
    val records: List<GLSRecord> = emptyList(),
    val requestStatus: RequestStatus = RequestStatus.IDLE
)