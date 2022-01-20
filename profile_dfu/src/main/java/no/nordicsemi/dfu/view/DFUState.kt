package no.nordicsemi.dfu.view

import no.nordicsemi.dfu.data.DFUData

internal sealed class DFUViewState

internal object LoadingState : DFUViewState()

internal data class DisplayDataState(val data: DFUData) : DFUViewState()
