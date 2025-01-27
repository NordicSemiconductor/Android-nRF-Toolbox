package no.nordicsemi.android.toolbox.profile

import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination

val DeviceConnectionDestinationId = createDestination<String, Unit>("connect-device-destination")
val DeviceConnectionDestination = listOf(
    defineDestination(DeviceConnectionDestinationId) {
        DeviceConnectionScreen()
    }
)
