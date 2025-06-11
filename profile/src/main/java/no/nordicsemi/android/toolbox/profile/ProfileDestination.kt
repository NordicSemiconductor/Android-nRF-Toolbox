package no.nordicsemi.android.toolbox.profile

import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination

val ProfileDestinationId = createDestination<String, Unit>("profile-destination")
val ProfileDestination = listOf(
    defineDestination(ProfileDestinationId) {
        ProfileScreen()
    }
)
