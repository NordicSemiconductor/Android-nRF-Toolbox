package no.nordicsemi.android.hts.view

import no.nordicsemi.android.hts.data.HTSData

internal sealed class HTSViewState

internal object LoadingState : HTSViewState()

internal data class DisplayDataState(val data: HTSData) : HTSViewState()
