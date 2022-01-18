package no.nordicsemi.android.rscs.view

import no.nordicsemi.android.rscs.data.RSCSData

internal data class RSCSState(
    val viewState: RSCSViewState,
    val isActive: Boolean = true
)

internal sealed class RSCSViewState

internal object LoadingState : RSCSViewState()

internal data class DisplayDataState(val data: RSCSData) : RSCSViewState()
