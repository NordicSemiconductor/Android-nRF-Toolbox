package no.nordicsemi.android.toolbox.libs.profile.gls

import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSMeasurementContext
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSRecord
import no.nordicsemi.android.toolbox.libs.profile.gls.details.view.GLSDetailsScreen

internal val GlsDetailsDestinationId =
    createDestination<Pair<GLSRecord, GLSMeasurementContext?>, Unit>("gls-details-screen")

internal val GLSDestination = defineDestination(GlsDetailsDestinationId) { GLSDetailsScreen() }