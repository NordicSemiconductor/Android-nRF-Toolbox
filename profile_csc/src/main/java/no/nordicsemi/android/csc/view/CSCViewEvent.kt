package no.nordicsemi.android.csc.view

internal sealed class CSCViewEvent

internal object OnShowEditWheelSizeDialogButtonClick : CSCViewEvent()

internal data class OnWheelSizeSelected(val wheelSize: Int, val wheelSizeDisplayInfo: String) : CSCViewEvent()

internal object OnCloseSelectWheelSizeDialog : CSCViewEvent()

internal data class OnSelectedSpeedUnitSelected(val selectedSpeedUnit: SpeedUnit) : CSCViewEvent()

internal object OnDisconnectButtonClick : CSCViewEvent()
