package no.nordicsemi.android.toolbox.libs.profile.view.gls

import android.os.Parcelable
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSMeasurementContext
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSRecord
import no.nordicsemi.android.toolbox.libs.profile.view.gls.details.GLSDetailsScreen

@Parcelize
data class GlsDetailsDestinationArgs(
    val deviceId: String,
    val data: Pair<GLSRecord, GLSMeasurementContext?>
) : Parcelable

internal val GlsDetailsDestinationId =
    createDestination<GlsDetailsDestinationArgs, Unit>("gls-details-screen")

internal val GLSDestination = defineDestination(GlsDetailsDestinationId) {
    val simpleNavigationViewModel: SimpleNavigationViewModel = hiltViewModel()
    val params = simpleNavigationViewModel.parameterOf(GlsDetailsDestinationId)

    GLSDetailsScreen(
        deviceId = params.deviceId,
        recordDetails = params.data,
        onBackClick = { simpleNavigationViewModel.navigateUp() }
    )
}
