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
data class GLSDetailsDestinationArgs(
    val deviceId: String,
    val data: Pair<GLSRecord, GLSMeasurementContext?>
) : Parcelable

internal val GLSDetailsDestinationId =
    createDestination<GLSDetailsDestinationArgs, Unit>("gls-details-screen")

internal val GLSDetailsDestination = defineDestination(GLSDetailsDestinationId) {
    val simpleNavigationViewModel: SimpleNavigationViewModel = hiltViewModel()
    val params = simpleNavigationViewModel.parameterOf(GLSDetailsDestinationId)

    GLSDetailsScreen(
        deviceId = params.deviceId,
        recordDetails = params.data,
        onBackClick = { simpleNavigationViewModel.navigateUp() }
    )
}
