package no.nordicsemi.android.cgms.view

import no.nordicsemi.android.cgms.data.CGMData
import no.nordicsemi.android.service.BleManagerResult

internal sealed class BPSViewState

internal data class WorkingState(val result: BleManagerResult<CGMData>) : BPSViewState()
internal object NoDeviceState : BPSViewState()
