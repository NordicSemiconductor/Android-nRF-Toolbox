package no.nordicsemi.android.rscs.view

import no.nordicsemi.android.rscs.data.RSCSData
import no.nordicsemi.android.service.BleManagerResult

internal sealed class RSCSViewState

internal data class WorkingState(val result: BleManagerResult<RSCSData>) : RSCSViewState()

internal object NoDeviceState : RSCSViewState()
