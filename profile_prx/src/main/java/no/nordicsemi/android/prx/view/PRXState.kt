package no.nordicsemi.android.prx.view

import no.nordicsemi.android.prx.data.PRXData

internal data class PRXState(
    val viewState: PRXViewState,
    val isActive: Boolean = true
)

internal sealed class PRXViewState

internal object LoadingState : PRXViewState()

internal data class DisplayDataState(val data: PRXData) : PRXViewState()
