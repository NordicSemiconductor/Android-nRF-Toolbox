package no.nordicsemi.android.toolbox.libs.profile

import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.toolbox.libs.profile.view.gls.GLSDetailsDestination

val DeviceConnectionDestinationId = createDestination<String, Unit>("connect-device-destination")
val DeviceConnectionDestination = listOf(
    defineDestination(DeviceConnectionDestinationId) {
        DeviceConnectionScreen()
    },
    GLSDetailsDestination
)
