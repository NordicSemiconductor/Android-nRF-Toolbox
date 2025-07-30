package no.nordicsemi.android.toolbox.profile.parser.directionFinder

import no.nordicsemi.android.toolbox.profile.parser.directionFinder.controlPoint.ControlPointMode
import no.nordicsemi.android.toolbox.profile.parser.directionFinder.distance.DistanceMode

fun ControlPointMode.toDistanceMode(): DistanceMode {
    return when (this) {
        ControlPointMode.RTT -> DistanceMode.RTT
        ControlPointMode.MCPD -> DistanceMode.MCPD
    }
}