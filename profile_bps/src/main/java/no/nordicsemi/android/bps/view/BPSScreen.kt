package no.nordicsemi.android.bps.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.bps.R
import no.nordicsemi.android.bps.viewmodel.BPSViewModel
import no.nordicsemi.android.service.*
import no.nordicsemi.android.theme.view.LoggerIconAppBar
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.ui.scanner.ui.DeviceConnectingView
import no.nordicsemi.ui.scanner.ui.DeviceDisconnectedView
import no.nordicsemi.ui.scanner.ui.NoDeviceView
import no.nordicsemi.ui.scanner.ui.Reason

@Composable
fun BPSScreen() {
    val viewModel: BPSViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    Column {
        val navigateUp = { viewModel.onEvent(DisconnectEvent) }

        LoggerIconAppBar(stringResource(id = R.string.bps_title), {
            viewModel.onEvent(DisconnectEvent)
        }) {
            viewModel.onEvent(OpenLoggerEvent)
        }

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            when (state) {
                NoDeviceState -> NoDeviceView()
                is WorkingState -> when (state.result) {
                    is IdleResult,
                    is ConnectingResult -> DeviceConnectingView { viewModel.onEvent(DisconnectEvent) }
                    is DisconnectedResult -> DeviceDisconnectedView(Reason.USER, navigateUp)
                    is LinkLossResult -> DeviceDisconnectedView(Reason.LINK_LOSS, navigateUp)
                    is MissingServiceResult -> DeviceDisconnectedView(Reason.MISSING_SERVICE, navigateUp)
                    is UnknownErrorResult -> DeviceDisconnectedView(Reason.UNKNOWN, navigateUp)
                    is SuccessResult -> BPSContentView(state.result.data) { viewModel.onEvent(it) }
                }
            }.exhaustive
        }
    }
}
