package no.nordicsemi.android.gls.data

import no.nordicsemi.android.theme.view.RadioGroupItem

internal data class GLSData(
    val records: List<GLSRecord> = emptyList(),
    val batteryLevel: Int = 0,
    val requestStatus: RequestStatus = RequestStatus.IDLE,
    val isDeviceBonded: Boolean = false,
    val selectedMode: WorkingMode = WorkingMode.ALL
) {
    fun modeItems(): List<RadioGroupItem<WorkingMode>> {
        return listOf(
            RadioGroupItem(WorkingMode.ALL, "All"),
            RadioGroupItem(WorkingMode.FIRST, "First"),
            RadioGroupItem(WorkingMode.LAST, "Last")
        )
    }
}

internal enum class WorkingMode {
    ALL, LAST, FIRST
}

internal enum class RequestStatus {
    IDLE, PENDING, SUCCESS, ABORTED, FAILED, NOT_SUPPORTED
}
