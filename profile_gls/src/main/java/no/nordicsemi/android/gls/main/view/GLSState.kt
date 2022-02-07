package no.nordicsemi.android.gls.main.view

import no.nordicsemi.android.gls.data.GLSData
import no.nordicsemi.android.service.BleManagerResult

internal sealed class BPSViewState

internal data class WorkingState(val result: BleManagerResult<GLSData>) : BPSViewState()
internal object NoDeviceState : BPSViewState()
