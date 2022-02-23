package no.nordicsemi.android.uart.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.service.*
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.theme.view.scanner.DeviceConnectingView
import no.nordicsemi.android.theme.view.scanner.DeviceDisconnectedView
import no.nordicsemi.android.theme.view.scanner.NoDeviceView
import no.nordicsemi.android.theme.view.scanner.Reason
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.viewmodel.UARTViewModel
import no.nordicsemi.android.utils.exhaustive

@Composable
fun UARTScreen() {
    val viewModel: UARTViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    if (state.showEditDialog) {
        UARTAddMacroDialog(state.selectedMacro) { viewModel.onEvent(it) }
    }

    Column {
        val navigateUp = { viewModel.onEvent(NavigateUp) }

        BackIconAppBar(stringResource(id = R.string.uart_title), navigateUp)

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            when (state.uartManagerState) {
                NoDeviceState -> NoDeviceView()
                is WorkingState -> when (state.uartManagerState.result) {
                    is ConnectingResult -> DeviceConnectingView { viewModel.onEvent(DisconnectEvent) }
                    is DisconnectedResult -> DeviceDisconnectedView(Reason.USER, navigateUp)
                    is LinkLossResult -> DeviceDisconnectedView(Reason.LINK_LOSS, navigateUp)
                    is MissingServiceResult -> DeviceDisconnectedView(Reason.MISSING_SERVICE, navigateUp)
                    is UnknownErrorResult -> DeviceDisconnectedView(Reason.UNKNOWN, navigateUp)
                    is SuccessResult -> UARTContentView(state.uartManagerState.result.data, state) { viewModel.onEvent(it) }
                }
            }.exhaustive
        }
    }
}
