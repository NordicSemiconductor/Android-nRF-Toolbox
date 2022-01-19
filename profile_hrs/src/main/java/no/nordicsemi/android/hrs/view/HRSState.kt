package no.nordicsemi.android.hrs.view

import no.nordicsemi.android.hrs.data.HRSData

internal sealed class HRSViewState

internal object LoadingState : HRSViewState()

internal data class DisplayDataState(val data: HRSData) : HRSViewState()
