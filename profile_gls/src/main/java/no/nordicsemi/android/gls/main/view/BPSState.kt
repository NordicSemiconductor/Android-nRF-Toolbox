package no.nordicsemi.android.gls.main.view

import no.nordicsemi.android.gls.data.GLSData

internal sealed class GLSViewState

internal object LoadingState : GLSViewState()

internal data class DisplayDataState(val data: GLSData) : GLSViewState()
