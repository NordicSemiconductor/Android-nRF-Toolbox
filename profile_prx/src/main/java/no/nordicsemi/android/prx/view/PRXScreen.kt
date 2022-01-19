package no.nordicsemi.android.prx.view

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.prx.R
import no.nordicsemi.android.prx.service.PRXService
import no.nordicsemi.android.prx.viewmodel.PRXViewModel
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.theme.view.DeviceConnectingView
import no.nordicsemi.android.utils.exhaustive

@Composable
fun PRXScreen() {
    val viewModel: PRXViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val intent = Intent(context, PRXService::class.java)
        context.startService(intent)
    }

    PRXView(state) { viewModel.onEvent(it) }
}

@Composable
private fun PRXView(state: PRXViewState, onEvent: (PRXScreenViewEvent) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        BackIconAppBar(stringResource(id = R.string.prx_title)) {
            onEvent(DisconnectEvent)
        }

        when (state) {
            is DisplayDataState -> ContentView(state.data) { onEvent(it) }
            LoadingState -> DeviceConnectingView()
        }.exhaustive
    }
}
