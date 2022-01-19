package no.nordicsemi.android.hrs.view

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.hrs.R
import no.nordicsemi.android.hrs.service.HRSService
import no.nordicsemi.android.hrs.viewmodel.HRSViewModel
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.theme.view.DeviceConnectingView
import no.nordicsemi.android.utils.exhaustive

@Composable
fun HRSScreen() {
    val viewModel: HRSViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val intent = Intent(context, HRSService::class.java)
        context.startService(intent)
    }

    HRSView(state) { viewModel.onEvent(it) }
}

@Composable
private fun HRSView(state: HRSViewState, onEvent: (HRSScreenViewEvent) -> Unit) {
    Column {
        BackIconAppBar(stringResource(id = R.string.hrs_title)) {
            onEvent(DisconnectEvent)
        }

        when (state) {
            is DisplayDataState -> HRSContentView(state.data) { onEvent(it) }
            LoadingState -> DeviceConnectingView()
        }.exhaustive
    }
}
