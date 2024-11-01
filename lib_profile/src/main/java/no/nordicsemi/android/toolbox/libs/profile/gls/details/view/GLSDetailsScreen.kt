package no.nordicsemi.android.toolbox.libs.profile.gls.details.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.profile.gls.details.viewmodel.GLSDetailsViewModel
import no.nordicsemi.android.ui.view.LoggerBackIconAppBar

@Composable
internal fun GLSDetailsScreen() {
    val viewModel: GLSDetailsViewModel = hiltViewModel()
    val record by viewModel.record.collectAsStateWithLifecycle()

    Column {
        LoggerBackIconAppBar(stringResource(id = R.string.gls_title)) {
            viewModel.navigateBack()
        }

        GLSDetailsContentView(record.first, record.second)
    }
}