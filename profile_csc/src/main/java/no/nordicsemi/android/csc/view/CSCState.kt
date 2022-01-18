package no.nordicsemi.android.csc.view

import no.nordicsemi.android.csc.data.CSCData

internal data class CSCState(
    val viewState: CSCViewState,
    val isActive: Boolean = true
)

internal sealed class CSCViewState

internal object LoadingState : CSCViewState()

internal data class DisplayDataState(val data: CSCData) : CSCViewState()
