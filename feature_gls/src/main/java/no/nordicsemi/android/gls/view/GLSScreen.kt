package no.nordicsemi.android.gls.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.gls.R
import no.nordicsemi.android.gls.data.GLSData
import no.nordicsemi.android.gls.viewmodel.GLSScreenViewEvent
import no.nordicsemi.android.gls.viewmodel.GLSViewModel

@Composable
fun GLSScreen(finishAction: () -> Unit) {
    val viewModel: GLSViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value
}

@Composable
private fun GLSView(state: GLSData, onEvent: (GLSScreenViewEvent) -> Unit) {
    Column {
        TopAppBar(title = { Text(text = stringResource(id = R.string.gls_title)) })

        GLSContentView(state, onEvent)
    }
}
