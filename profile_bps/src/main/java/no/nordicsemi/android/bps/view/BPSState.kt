package no.nordicsemi.android.bps.view

import no.nordicsemi.android.bps.data.BPSData

internal sealed class BPSViewState

internal object LoadingState : BPSViewState()

internal data class DisplayDataState(val data: BPSData) : BPSViewState()
