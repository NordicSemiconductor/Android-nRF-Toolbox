package no.nordicsemi.android.toolbox.libs.profile.viewmodel

import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.toolbox.libs.core.data.common.WorkingMode
import no.nordicsemi.android.toolbox.libs.core.data.csc.SpeedUnit
import no.nordicsemi.android.toolbox.libs.core.data.csc.WheelSize
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.distance.DistanceMode
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.MeasurementSection
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.Range
import no.nordicsemi.android.toolbox.libs.core.data.hts.TemperatureUnit
import no.nordicsemi.android.toolbox.libs.core.data.rscs.RSCSSettingsUnit

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
