package no.nordicsemi.android.toolbox.libs.profile

import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination

val DeviceConnectionDestinationId = createDestination<String, Unit>("connect-device-destination")
val DeviceConnectionDestination = defineDestination(DeviceConnectionDestinationId) {
    DeviceConnectionScreen()
}
