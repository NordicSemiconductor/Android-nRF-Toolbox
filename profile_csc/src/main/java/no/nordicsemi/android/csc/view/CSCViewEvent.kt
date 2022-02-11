package no.nordicsemi.android.csc.view

import no.nordicsemi.android.csc.data.WheelSize

internal sealed class CSCViewEvent

internal data class OnWheelSizeSelected(val wheelSize: WheelSize) : CSCViewEvent()

internal data class OnSelectedSpeedUnitSelected(val selectedSpeedUnit: SpeedUnit) : CSCViewEvent()

internal object OnDisconnectButtonClick : CSCViewEvent()

internal object NavigateUp : CSCViewEvent()
