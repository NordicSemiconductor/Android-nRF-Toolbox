package no.nordicsemi.android.hts.view

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.hts.R
import no.nordicsemi.android.hts.repository.HTSService
import no.nordicsemi.android.hts.viewmodel.HTSViewModel
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.theme.view.DeviceConnectingView
import no.nordicsemi.android.utils.exhaustive

@Composable
fun HTSScreen(finishAction: () -> Unit) {
    val viewModel: HTSViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    val context = LocalContext.current
    LaunchedEffect(state.isActive) {
        if (state.isActive) {
            val intent = Intent(context, HTSService::class.java)
            context.startService(intent)
        } else {
            finishAction()
        }
    }

    HTSView(state.viewState) { viewModel.onEvent(it) }
}

@Composable
private fun HTSView(state: HTSViewState, onEvent: (HTSScreenViewEvent) -> Unit) {
    Column {
        BackIconAppBar(stringResource(id = R.string.hts_title)) {
            onEvent(DisconnectEvent)
        }

        when (state) {
            is DisplayDataState -> HTSContentView(state.data) { onEvent(it) }
            LoadingState -> DeviceConnectingView()
        }.exhaustive
    }
}
