package no.nordicsemi.android.gls

import no.nordicsemi.android.gls.details.view.GLSDetailsScreen
import no.nordicsemi.android.navigation.ComposeDestination
import no.nordicsemi.android.navigation.ComposeDestinations
import no.nordicsemi.android.navigation.DestinationId

internal val GlsDetailsDestinationId = DestinationId("gls-details-screen")

private val destination: ComposeDestination = ComposeDestination(GlsDetailsDestinationId) { GLSDetailsScreen() }

val GLSDestinations = ComposeDestinations(destination)
