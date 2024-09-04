package no.nordicsemi.android.hts

import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.hts.view.HtsHomeScreen

val HTSDestinationId = createDestination<String, Unit>("hts-destination")

val HTSDestination = defineDestination(HTSDestinationId) { HtsHomeScreen() }
