package no.nordicsemi.android.hts.view

import no.nordicsemi.android.hts.data.HTSData
import no.nordicsemi.android.service.BleManagerResult

internal data class HTSViewState(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val htsManagerState: HTSManagerState = NoDeviceState
)

internal sealed class HTSManagerState

internal data class WorkingState(val result: BleManagerResult<HTSData>) : HTSManagerState()

internal object NoDeviceState : HTSManagerState()
