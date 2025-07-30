package no.nordicsemi.android.toolbox.profile.parser.directionFinder.controlPoint

sealed class ControlPointResult

data class ControlPointCheckModeSuccess(
    val mode: ControlPointMode
) : ControlPointResult()

data object ControlPointCheckModeError : ControlPointResult()

data class ControlPointChangeModeSuccess(
    val mode: ControlPointMode
) : ControlPointResult()

data object ControlPointChangeModeError : ControlPointResult()
