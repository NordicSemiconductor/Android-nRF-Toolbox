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
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.data.UARTData
import no.nordicsemi.android.uart.repository.UARTService
import no.nordicsemi.android.uart.viewmodel.UARTViewModel
import no.nordicsemi.android.utils.isServiceRunning

@Composable
fun UARTScreen(finishAction: () -> Unit) {
    val viewModel: UARTViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value
    val isScreenActive = viewModel.isActive.collectAsState().value

    val context = LocalContext.current
    LaunchedEffect(isScreenActive) {
        if (!isScreenActive) {
            finishAction()
        }
        if (context.isServiceRunning(UARTService::class.java.name)) {
            val intent = Intent(context, UARTService::class.java)
            context.stopService(intent)
        }
    }

    LaunchedEffect("start-service") {
        if (!context.isServiceRunning(UARTService::class.java.name)) {
            val intent = Intent(context, UARTService::class.java)
            context.startService(intent)
        }
    }

    UARTView(state) { viewModel.onEvent(it) }
}

@Composable
private fun UARTView(state: UARTData, onEvent: (UARTViewEvent) -> Unit) {
    Column {
        BackIconAppBar(stringResource(id = R.string.uart_title)) {
            onEvent(OnDisconnectButtonClick)
        }

        UARTContentView(state) { onEvent(it) }
    }
}
