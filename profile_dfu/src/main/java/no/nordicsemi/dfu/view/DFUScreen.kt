package no.nordicsemi.dfu.view

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.theme.view.DeviceConnectingView
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.dfu.R
import no.nordicsemi.dfu.repository.DFUService
import no.nordicsemi.dfu.viewmodel.DFUViewModel

@Composable
fun DFUScreen(finishAction: () -> Unit) {
    val viewModel: DFUViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    val context = LocalContext.current
    LaunchedEffect(state.isActive) {
        if (state.isActive) {
            val intent = Intent(context, DFUService::class.java)
            context.startService(intent)
        } else {
            finishAction()
        }
    }

    DFUView(state.viewState) { viewModel.onEvent(it) }
}

@Composable
private fun DFUView(state: DFUViewState, onEvent: (DFUViewEvent) -> Unit) {
    Column {
        BackIconAppBar(stringResource(id = R.string.dfu_title)) {
            onEvent(OnDisconnectButtonClick)
        }

        when (state) {
            is DisplayDataState -> DFUContentView(state.data) { onEvent(it) }
            LoadingState -> DeviceConnectingView()
        }.exhaustive
    }
}
