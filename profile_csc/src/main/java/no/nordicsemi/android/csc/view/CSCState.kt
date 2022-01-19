package no.nordicsemi.android.csc.view

import no.nordicsemi.android.csc.data.CSCData

internal sealed class CSCViewState

internal object LoadingState : CSCViewState()

internal data class DisplayDataState(val data: CSCData) : CSCViewState()
