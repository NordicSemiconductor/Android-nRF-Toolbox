package no.nordicsemi.android.toolbox.libs.profile.viewmodel

import no.nordicsemi.android.toolbox.libs.core.data.gls.WorkingMode
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSMeasurementContext
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSRecord
import no.nordicsemi.android.toolbox.libs.core.data.hts.TemperatureUnit

sealed interface DeviceConnectionViewEvent

internal data class OnTemperatureUnitSelected(
    val value: TemperatureUnit,
) : DeviceConnectionViewEvent

internal data object SwitchZoomEvent : DeviceConnectionViewEvent

internal data class OnRetryClicked(val device: String) : DeviceConnectionViewEvent

internal data object NavigateUp : DeviceConnectionViewEvent

internal data class DisconnectEvent(val device: String) : DeviceConnectionViewEvent

internal data object OpenLoggerEvent : DeviceConnectionViewEvent

internal class OnWorkingModeSelected(
    val workingMode: WorkingMode,
) : DeviceConnectionViewEvent

internal data class OnGLSRecordClick(
    val record: GLSRecord,
    val gleContext: GLSMeasurementContext?,
    ) : DeviceConnectionViewEvent
