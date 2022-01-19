package no.nordicsemi.android.prx.view

import no.nordicsemi.android.prx.data.PRXData

internal sealed class PRXViewState

internal object LoadingState : PRXViewState()

internal data class DisplayDataState(val data: PRXData) : PRXViewState()
