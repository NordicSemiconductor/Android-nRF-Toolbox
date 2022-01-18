package no.nordicsemi.android.gls.view

import no.nordicsemi.android.gls.data.GLSData

internal data class GLSState(
    val viewState: GLSViewState,
    val isActive: Boolean = true
)

internal sealed class GLSViewState

internal object LoadingState : GLSViewState()

internal data class DisplayDataState(val data: GLSData) : GLSViewState()
