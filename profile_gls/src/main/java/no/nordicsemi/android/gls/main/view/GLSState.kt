package no.nordicsemi.android.gls.main.view

import no.nordicsemi.android.gls.data.GLSData
import no.nordicsemi.android.service.BleManagerResult

internal sealed class GLSViewState

internal data class WorkingState(val result: BleManagerResult<GLSData>) : GLSViewState()
internal object NoDeviceState : GLSViewState()
