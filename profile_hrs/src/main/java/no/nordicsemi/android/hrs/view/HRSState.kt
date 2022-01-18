package no.nordicsemi.android.hrs.view

import no.nordicsemi.android.hrs.data.HRSData

internal data class HRSState(
    val viewState: HRSViewState,
    val isActive: Boolean = true
)

internal sealed class HRSViewState

internal object LoadingState : HRSViewState()

internal data class DisplayDataState(val data: HRSData) : HRSViewState()
