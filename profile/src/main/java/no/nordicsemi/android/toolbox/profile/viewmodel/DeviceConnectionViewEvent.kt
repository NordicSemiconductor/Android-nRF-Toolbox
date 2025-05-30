package no.nordicsemi.android.toolbox.profile.viewmodel

import no.nordicsemi.android.lib.profile.common.WorkingMode
import no.nordicsemi.android.lib.profile.csc.SpeedUnit
import no.nordicsemi.android.lib.profile.csc.WheelSize
import no.nordicsemi.android.lib.profile.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.lib.profile.directionFinder.distance.DistanceMode
import no.nordicsemi.android.lib.profile.rscs.RSCSSettingsUnit
import no.nordicsemi.android.toolbox.profile.data.Profile
import no.nordicsemi.android.toolbox.profile.data.ThroughputInputType
import no.nordicsemi.android.toolbox.profile.data.directionFinder.MeasurementSection
import no.nordicsemi.android.toolbox.profile.data.directionFinder.Range
import no.nordicsemi.android.toolbox.profile.data.uart.MacroEol
import no.nordicsemi.android.toolbox.profile.data.uart.UARTConfiguration
import no.nordicsemi.android.toolbox.profile.data.uart.UARTMacro
import no.nordicsemi.android.toolbox.profile.data.uiMapper.TemperatureUnit

sealed interface DeviceConnectionViewEvent

// HTS Profile Events
internal sealed interface HTSViewEvent : DeviceConnectionViewEvent {
    data class OnTemperatureUnitSelected(val value: TemperatureUnit) : HTSViewEvent
}

// HRS Profile Events
sealed interface HRSViewEvent : DeviceConnectionViewEvent {
    data object SwitchZoomEvent : HRSViewEvent
}

internal data class OnRetryClicked(val device: String) : DeviceConnectionViewEvent

internal data object NavigateUp : DeviceConnectionViewEvent

internal data class DisconnectEvent(val device: String) : DeviceConnectionViewEvent

internal data object OpenLoggerEvent : DeviceConnectionViewEvent

// GLS/CGM Profile Events
internal sealed interface GLSViewEvent : DeviceConnectionViewEvent {
    data class OnWorkingModeSelected(
        val profile: Profile,
        val workingMode: WorkingMode
    ) : GLSViewEvent

}

// CSC Profile Events
internal sealed interface CSCViewEvent : DeviceConnectionViewEvent {
    data class OnWheelSizeSelected(val wheelSize: WheelSize) : CSCViewEvent
    data class OnSelectedSpeedUnitSelected(val selectedSpeedUnit: SpeedUnit) : CSCViewEvent
}

// RSCS Profile Events
internal sealed interface RSCSViewEvent : DeviceConnectionViewEvent {
    data class OnSelectedSpeedUnitSelected(val rscsUnitSettings: RSCSSettingsUnit) : RSCSViewEvent
}

internal sealed interface DFSViewEvent : DeviceConnectionViewEvent {
    data object OnAvailableDistanceModeRequest : DFSViewEvent
    data object OnCheckDistanceModeRequest : DFSViewEvent
    data class OnRangeChangedEvent(val range: Range) : DFSViewEvent
    data class OnDistanceModeSelected(val mode: DistanceMode) : DFSViewEvent
    data class OnDetailsSectionParamsSelected(val section: MeasurementSection) : DFSViewEvent
    data class OnBluetoothDeviceSelected(val device: PeripheralBluetoothAddress) : DFSViewEvent
}

internal sealed interface ThroughputEvent : DeviceConnectionViewEvent {
    data class OnWriteData(
        val writeType: ThroughputInputType,
    ) : ThroughputEvent

}

// UART Profile events.
internal sealed interface UARTEvent : DeviceConnectionViewEvent {
    data class OnCreateMacro(
        val macroName: UARTMacro,
    ) : UARTEvent

    data class OnEditMacro(
        val position: Int,
    ) : UARTEvent

    data object OnEditFinished : UARTEvent
    data object OnDeleteMacro : UARTEvent

    data class OnRunMacro(
        val macro: UARTMacro,
    ) : UARTEvent

    data class OnConfigurationSelected(
        val configuration: UARTConfiguration,
    ) : UARTEvent

    data class OnAddConfiguration(
        val name: String,
    ) : UARTEvent

    data object OnEditConfiguration : UARTEvent
    data class OnDeleteConfiguration(
        val configuration: UARTConfiguration,
    ) : UARTEvent

    data class OnRunInput(
        val text: String,
        val newLineChar: MacroEol,
    ) : UARTEvent

    data object ClearOutputItems : UARTEvent
    data object MacroInputSwitchClicked : UARTEvent

}

// LBS Profile events.
internal sealed interface LBSViewEvent : DeviceConnectionViewEvent {
    data class OnLedStateChanged(val ledState: Boolean) : LBSViewEvent
    data class OnButtonStateChanged(val buttonState: Boolean) : LBSViewEvent
}
