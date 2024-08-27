package no.nordicsemi.android.hts

import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.hts.view.HtsHomeScreen

val HTSDestinationId = createSimpleDestination("hts-destination")

val HTSDestination = defineDestination(HTSDestinationId) { HtsHomeScreen() }
