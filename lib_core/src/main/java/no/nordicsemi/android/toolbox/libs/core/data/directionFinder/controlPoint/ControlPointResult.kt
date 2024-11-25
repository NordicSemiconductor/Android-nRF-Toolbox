package no.nordicsemi.android.toolbox.libs.core.data.directionFinder.controlPoint

sealed class ControlPointResult

data class ControlPointCheckModeSuccess(
    val mode: ControlPointMode
) : ControlPointResult()

data object ControlPointCheckModeError : ControlPointResult()

data object ControlPointChangeModeSuccess : ControlPointResult()

data object ControlPointChangeModeError : ControlPointResult()
