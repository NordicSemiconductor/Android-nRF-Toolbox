package no.nordicsemi.android.csc.view

import android.bluetooth.BluetoothDevice

internal sealed class CSCViewEvent

internal object OnShowEditWheelSizeDialogButtonClick : CSCViewEvent()

internal data class OnWheelSizeSelected(val wheelSize: Int, val wheelSizeDisplayInfo: String) : CSCViewEvent()

internal data class OnSelectedSpeedUnitSelected(val selectedSpeedUnit: SpeedUnit) : CSCViewEvent()

internal object OnDisconnectButtonClick : CSCViewEvent()

internal object OnConnectButtonClick : CSCViewEvent()

internal object OnMovedToScannerScreen : CSCViewEvent()

internal data class OnBluetoothDeviceSelected(val device: BluetoothDevice) : CSCViewEvent()
