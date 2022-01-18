package no.nordicsemi.android.csc.view

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.csc.R
import no.nordicsemi.android.csc.repository.CSCService
import no.nordicsemi.android.csc.viewmodel.CSCViewModel
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.theme.view.DeviceConnectingView
import no.nordicsemi.android.utils.exhaustive

@Composable
fun CSCScreen(finishAction: () -> Unit) {
    val viewModel: CSCViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    val context = LocalContext.current
    LaunchedEffect(state.isActive) {
        if (state.isActive) {
            val intent = Intent(context, CSCService::class.java)
            context.startService(intent)
        } else {
            finishAction()
        }
    }

    CSCView(state.viewState) { viewModel.onEvent(it) }
}

@Composable
private fun CSCView(state: CSCViewState, onEvent: (CSCViewEvent) -> Unit) {
    Column {
        BackIconAppBar(stringResource(id = R.string.csc_title)) {
            onEvent(OnDisconnectButtonClick)
        }

        when (state) {
            is DisplayDataState -> CSCContentView(state.data, onEvent)
            LoadingState -> DeviceConnectingView()
        }.exhaustive
    }
}
