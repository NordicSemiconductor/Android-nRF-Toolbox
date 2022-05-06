package no.nordicsemi.android.uart.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.material.you.PagerView
import no.nordicsemi.android.material.you.PagerViewEntity
import no.nordicsemi.android.material.you.PagerViewItem
import no.nordicsemi.android.service.*
import no.nordicsemi.android.theme.view.LoggerIconAppBar
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.data.UARTData
import no.nordicsemi.android.uart.viewmodel.UARTViewModel
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.ui.scanner.ui.DeviceConnectingView
import no.nordicsemi.ui.scanner.ui.DeviceDisconnectedView
import no.nordicsemi.ui.scanner.ui.NoDeviceView
import no.nordicsemi.ui.scanner.ui.Reason

@Composable
fun UARTScreen() {
    val viewModel: UARTViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    Column {
        val navigateUp = { viewModel.onEvent(NavigateUp) }

        LoggerIconAppBar(stringResource(id = R.string.uart_title), navigateUp, { viewModel.onEvent(DisconnectEvent) }) {
            viewModel.onEvent(OpenLogger)
        }

        Column(modifier = Modifier) {
            when (state.uartManagerState) {
                NoDeviceState -> NoDeviceView()
                is WorkingState -> when (state.uartManagerState.result) {
                    is IdleResult,
                    is ConnectingResult -> Scroll { DeviceConnectingView { viewModel.onEvent(DisconnectEvent) } }
                    is DisconnectedResult -> Scroll { DeviceDisconnectedView(Reason.USER, navigateUp) }
                    is LinkLossResult -> Scroll { DeviceDisconnectedView(Reason.LINK_LOSS, navigateUp) }
                    is MissingServiceResult -> Scroll { DeviceDisconnectedView(Reason.MISSING_SERVICE, navigateUp) }
                    is UnknownErrorResult -> Scroll { DeviceDisconnectedView(Reason.UNKNOWN, navigateUp) }
                    is SuccessResult -> SuccessScreen(state.uartManagerState.result.data, state, viewModel)
                }
                TutorialState -> TutorialScreen(viewModel)
            }.exhaustive
        }
    }
}

@Composable
private fun SuccessScreen(data: UARTData, state: UARTViewState, viewModel: UARTViewModel) {
    val viewEntity = PagerViewEntity(
        listOf(
            PagerViewItem(stringResource(id = R.string.uart_input)) {
                UARTContentView(data) { viewModel.onEvent(it) }
            },
            PagerViewItem(stringResource(id = R.string.uart_macros)) {
                MacroSection(state) { viewModel.onEvent(it) }
            }
        )
    )
    PagerView(viewEntity)
}

@Composable
fun Scroll(content: @Composable () -> Unit) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        content()
    }
}
