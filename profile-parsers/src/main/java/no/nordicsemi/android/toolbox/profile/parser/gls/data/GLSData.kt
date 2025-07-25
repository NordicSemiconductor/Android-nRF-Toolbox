package no.nordicsemi.android.toolbox.profile.parser.gls.data

internal data class GLSData(
    val records: List<GLSRecord> = emptyList(),
    val requestStatus: RequestStatus = RequestStatus.IDLE
)