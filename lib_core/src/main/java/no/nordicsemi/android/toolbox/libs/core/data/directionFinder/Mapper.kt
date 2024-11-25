package no.nordicsemi.android.toolbox.libs.core.data.directionFinder

import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.controlPoint.ControlPointMode
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.distance.DistanceMode

fun ControlPointMode.toDistanceMode(): DistanceMode {
    return when (this) {
        ControlPointMode.RTT -> DistanceMode.RTT
        ControlPointMode.MCPD -> DistanceMode.MCPD
    }
}