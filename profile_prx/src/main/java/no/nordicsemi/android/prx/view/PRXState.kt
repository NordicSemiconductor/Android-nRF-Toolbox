package no.nordicsemi.android.prx.view

import no.nordicsemi.android.prx.data.PRXData
import no.nordicsemi.android.service.BleManagerResult

internal sealed class PRXViewState

internal data class WorkingState(val result: BleManagerResult<PRXData>) : PRXViewState()

internal object NoDeviceState : PRXViewState()
