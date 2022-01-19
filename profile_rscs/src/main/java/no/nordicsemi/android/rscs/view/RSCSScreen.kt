package no.nordicsemi.android.rscs.view

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.rscs.R
import no.nordicsemi.android.rscs.service.RSCSService
import no.nordicsemi.android.rscs.viewmodel.RSCSViewModel
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.theme.view.DeviceConnectingView
import no.nordicsemi.android.utils.exhaustive

@Composable
fun RSCSScreen() {
    val viewModel: RSCSViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val intent = Intent(context, RSCSService::class.java)
        context.startService(intent)
    }

    RSCSView(state) { viewModel.onEvent(it) }
}

@Composable
private fun RSCSView(state: RSCSViewState, onEvent: (RSCScreenViewEvent) -> Unit) {
    Column {
        BackIconAppBar(stringResource(id = R.string.rscs_title)) {
            onEvent(DisconnectEvent)
        }

        when (state) {
            is DisplayDataState -> RSCSContentView(state.data) { onEvent(it) }
            LoadingState -> DeviceConnectingView()
        }.exhaustive
    }
}
