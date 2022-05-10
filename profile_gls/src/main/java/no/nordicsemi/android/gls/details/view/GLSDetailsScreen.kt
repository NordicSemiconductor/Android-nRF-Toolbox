package no.nordicsemi.android.gls.details.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.gls.R
import no.nordicsemi.android.gls.details.viewmodel.GLSDetailsViewModel
import no.nordicsemi.android.theme.view.LoggerBackIconAppBar

@Composable
internal fun GLSDetailsScreen() {
    val viewModel: GLSDetailsViewModel = hiltViewModel()
    val record = viewModel.record

    Column {
        LoggerBackIconAppBar(stringResource(id = R.string.gls_title)) {
            viewModel.navigateBack()
        }

        GLSDetailsContentView(record)
    }
}
