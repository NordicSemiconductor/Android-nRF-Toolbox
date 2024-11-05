package no.nordicsemi.android.toolbox.libs.profile.gls.details.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.toolbox.libs.profile.gls.details.viewmodel.GLSDetailsViewModel
import no.nordicsemi.android.ui.view.LoggerBackIconAppBar

@Composable
internal fun GLSDetailsScreen() {
    val viewModel: GLSDetailsViewModel = hiltViewModel()
    val glsDetailsDestinationParams by viewModel.record.collectAsStateWithLifecycle()

    Column {
        LoggerBackIconAppBar(glsDetailsDestinationParams.deviceId) {
            viewModel.navigateBack()
        }

        GLSDetailsContentView(
            glsDetailsDestinationParams.data.first,
            glsDetailsDestinationParams.data.second
        )
    }
}