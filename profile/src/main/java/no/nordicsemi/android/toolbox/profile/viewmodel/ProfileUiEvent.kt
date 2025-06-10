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

sealed interface ProfileUiEvent

// HTS Profile Events
internal sealed interface HTSEvent : ProfileUiEvent {
    data class OnTemperatureUnitSelected(val value: TemperatureUnit) : HTSEvent
}

// HRS Profile Events
sealed interface HRSEvent : ProfileUiEvent {
    data object SwitchZoomEvent : HRSEvent
}

internal data class OnRetryClicked(val device: String) : ProfileUiEvent

internal data object NavigateUp : ProfileUiEvent

internal data class DisconnectEvent(val device: String) : ProfileUiEvent

internal data object OpenLoggerEvent : ProfileUiEvent

// GLS/CGM Profile Events
internal sealed interface GLSEvent : ProfileUiEvent {
    data class OnWorkingModeSelected(
        val profile: Profile,
        val workingMode: WorkingMode
    ) : GLSEvent

}

// CSC Profile Events
internal sealed interface CSCEvent : ProfileUiEvent {
    data class OnWheelSizeSelected(val wheelSize: WheelSize) : CSCEvent
    data class OnSelectedSpeedUnitSelected(val selectedSpeedUnit: SpeedUnit) : CSCEvent
}

// RSCS Profile Events
internal sealed interface RSCSEvent : ProfileUiEvent {
    data class OnSelectedSpeedUnitSelected(val rscsUnitSettings: RSCSSettingsUnit) : RSCSEvent
}

internal sealed interface DFSEvent : ProfileUiEvent {
    data object OnAvailableDistanceModeRequest : DFSEvent
    data object OnCheckDistanceModeRequest : DFSEvent
    data class OnRangeChangedEvent(val range: Range) : DFSEvent
    data class OnDistanceModeSelected(val mode: DistanceMode) : DFSEvent
    data class OnDetailsSectionParamsSelected(val section: MeasurementSection) : DFSEvent
    data class OnBluetoothDeviceSelected(val device: PeripheralBluetoothAddress) : DFSEvent
}

internal sealed interface ThroughputEvent : ProfileUiEvent {
    data class OnWriteData(
        val writeType: ThroughputInputType,
    ) : ThroughputEvent

}

// UART Profile events.
internal sealed interface UARTEvent : ProfileUiEvent {
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

}

// LBS Profile events.
internal sealed interface LBSEvent : ProfileUiEvent {
    data class OnLedStateChanged(val ledState: Boolean) : LBSEvent
    data class OnButtonStateChanged(val buttonState: Boolean) : LBSEvent
}
