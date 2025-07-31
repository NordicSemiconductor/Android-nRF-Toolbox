package no.nordicsemi.android.lib.profile.directionFinder

import no.nordicsemi.android.lib.profile.directionFinder.controlPoint.ControlPointMode
import no.nordicsemi.android.lib.profile.directionFinder.distance.DistanceMode

fun ControlPointMode.toDistanceMode(): DistanceMode {
    return when (this) {
        ControlPointMode.RTT -> DistanceMode.RTT
        ControlPointMode.MCPD -> DistanceMode.MCPD
    }
}