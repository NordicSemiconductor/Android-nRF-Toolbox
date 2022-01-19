package no.nordicsemi.android.cgms.view

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.cgms.R
import no.nordicsemi.android.cgms.repository.CGMService
import no.nordicsemi.android.cgms.viewmodel.CGMScreenViewModel
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.theme.view.DeviceConnectingView
import no.nordicsemi.android.utils.exhaustive

@Composable
fun CGMScreen() {
    val viewModel: CGMScreenViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val intent = Intent(context, CGMService::class.java)
        context.startService(intent)
    }

    CGMView(state) {
        viewModel.onEvent(it)
    }
}

@Composable
private fun CGMView(state: CGMViewState, onEvent: (CGMViewEvent) -> Unit) {
    Column {
        BackIconAppBar(stringResource(id = R.string.cgms_title)) {
            onEvent(DisconnectEvent)
        }

        when (state) {
            is DisplayDataState -> CGMContentView(state.data, onEvent)
            LoadingState -> DeviceConnectingView()
        }.exhaustive
    }
}

