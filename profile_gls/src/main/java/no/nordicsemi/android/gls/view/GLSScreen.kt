package no.nordicsemi.android.gls.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.gls.R
import no.nordicsemi.android.gls.data.GLSData
import no.nordicsemi.android.gls.viewmodel.DisconnectEvent
import no.nordicsemi.android.gls.viewmodel.GLSScreenViewEvent
import no.nordicsemi.android.gls.viewmodel.GLSViewModel
import no.nordicsemi.android.theme.view.BackIconAppBar

@Composable
fun GLSScreen(finishAction: () -> Unit) {
    val viewModel: GLSViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value
    val isScreenActive = viewModel.isActive.collectAsState().value

    LaunchedEffect("connect") {
        viewModel.connectDevice()
    }

    LaunchedEffect(isScreenActive) {
        if (!isScreenActive) {
            finishAction()
        }
    }

    GLSView(state) {
        viewModel.onEvent(it)
    }
}

@Composable
private fun GLSView(state: GLSData, onEvent: (GLSScreenViewEvent) -> Unit) {
    Column {
        BackIconAppBar(stringResource(id = R.string.gls_title)) {
            onEvent(DisconnectEvent)
        }

        GLSContentView(state, onEvent)
    }
}
