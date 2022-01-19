package no.nordicsemi.android.rscs.view

import no.nordicsemi.android.rscs.data.RSCSData

internal sealed class RSCSViewState

internal object LoadingState : RSCSViewState()

internal data class DisplayDataState(val data: RSCSData) : RSCSViewState()
