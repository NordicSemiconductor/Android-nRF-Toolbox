package no.nordicsemi.android.cgms.view

import no.nordicsemi.android.cgms.data.CGMData

internal data class CGMState(
    val viewState: CGMViewState,
    val isActive: Boolean = true
)

internal sealed class CGMViewState

internal object LoadingState : CGMViewState()

internal data class DisplayDataState(val data: CGMData) : CGMViewState()
