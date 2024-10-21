package no.nordicsemi.android.toolbox.libs.profile.data.gls.data

internal data class GLSData(
    val records: List<GLSRecord> = emptyList(),
    val requestStatus: RequestStatus = RequestStatus.IDLE
)