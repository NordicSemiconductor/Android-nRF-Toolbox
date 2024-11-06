package no.nordicsemi.android.toolbox.libs.profile.view.gls.details

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSMeasurementContext
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSRecord
import no.nordicsemi.android.ui.view.LoggerBackIconAppBar

@Composable
internal fun GLSDetailsScreen(
    deviceId: String,
    recordDetails: Pair<GLSRecord, GLSMeasurementContext?>,
    onBackClick: () -> Unit
) {
    Column {
        LoggerBackIconAppBar(deviceId) {
            onBackClick()
        }

        GLSDetailsContentView(
            recordDetails.first,
            recordDetails.second
        )
    }
}