package no.nordicsemi.android.bps.view

import no.nordicsemi.android.bps.data.BPSData
import no.nordicsemi.android.service.BleManagerResult

internal sealed class BPSViewState

internal data class WorkingState(
    val result: BleManagerResult<BPSData>
) : BPSViewState()

internal object NoDeviceState : BPSViewState()
