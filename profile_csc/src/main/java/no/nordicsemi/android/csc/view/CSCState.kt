package no.nordicsemi.android.csc.view

import no.nordicsemi.android.csc.data.CSCData
import no.nordicsemi.android.service.BleManagerResult

internal data class CSCViewState(
    val speedUnit: SpeedUnit = SpeedUnit.M_S,
    val cscManagerState: CSCMangerState = NoDeviceState
)

internal sealed class CSCMangerState

internal data class WorkingState(val result: BleManagerResult<CSCData>) : CSCMangerState()

internal object NoDeviceState : CSCMangerState()
