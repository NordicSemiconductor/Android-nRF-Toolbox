package no.nordicsemi.android.uart.view

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
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.repository.UARTService
import no.nordicsemi.android.uart.viewmodel.UARTViewModel
import no.nordicsemi.android.utils.exhaustive

@Composable
fun UARTScreen() {
    val viewModel: UARTViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val intent = Intent(context, UARTService::class.java)
        context.startService(intent)
    }

    UARTView(state) { viewModel.onEvent(it) }
}

@Composable
private fun UARTView(state: UARTViewState, onEvent: (UARTViewEvent) -> Unit) {
    Column {
        BackIconAppBar(stringResource(id = R.string.uart_title)) {
            onEvent(OnDisconnectButtonClick)
        }

        when (state) {
            is DisplayDataState -> UARTContentView(state.data) { onEvent(it) }
            LoadingState -> DeviceConnectingView()
        }.exhaustive
    }
}
