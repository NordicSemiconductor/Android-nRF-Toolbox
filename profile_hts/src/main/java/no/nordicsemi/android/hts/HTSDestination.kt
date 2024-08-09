package no.nordicsemi.android.hts

import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.hts.view.HtsHomeView
import no.nordicsemi.android.toolbox.scanner.Profile

val HTSDestinationId =
    createDestination<Profile, Unit>("health-thermometer-service-destination")
val HTSDestination = defineDestination(HTSDestinationId) { HtsHomeView() }
