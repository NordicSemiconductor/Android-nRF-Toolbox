package no.nordicsemi.android.cgms.view

import no.nordicsemi.android.cgms.data.CGMData

internal sealed class CGMViewState

internal object LoadingState : CGMViewState()

internal data class DisplayDataState(val data: CGMData) : CGMViewState()
