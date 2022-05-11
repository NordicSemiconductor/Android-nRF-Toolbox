package no.nordicsemi.android.hts.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.hts.R
import no.nordicsemi.android.hts.data.HTSData
import no.nordicsemi.android.hts.viewmodel.HTSViewModel
import no.nordicsemi.android.service.*
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.theme.view.LoggerIconAppBar
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.ui.scanner.ui.DeviceConnectingView
import no.nordicsemi.ui.scanner.ui.DeviceDisconnectedView
import no.nordicsemi.ui.scanner.ui.NoDeviceView
import no.nordicsemi.ui.scanner.ui.Reason

@Composable
fun HTSScreen() {
    val viewModel: HTSViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    Column {
        val navigateUp = { viewModel.onEvent(NavigateUp) }

        AppBar(state, navigateUp, viewModel)

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            when (state.htsManagerState) {
                NoDeviceState -> NoDeviceView()
                is WorkingState -> when (state.htsManagerState.result) {
                    is IdleResult,
                    is ConnectingResult -> DeviceConnectingView { viewModel.onEvent(DisconnectEvent) }
                    is DisconnectedResult -> DeviceDisconnectedView(Reason.USER, navigateUp)
                    is LinkLossResult -> DeviceDisconnectedView(Reason.LINK_LOSS, navigateUp)
                    is MissingServiceResult -> DeviceDisconnectedView(Reason.MISSING_SERVICE, navigateUp)
                    is UnknownErrorResult -> DeviceDisconnectedView(Reason.UNKNOWN, navigateUp)
                    is SuccessResult -> HTSContentView(state.htsManagerState.result.data, state.temperatureUnit) { viewModel.onEvent(it) }
                }
            }.exhaustive
        }
    }
}

@Composable
private fun AppBar(state: HTSViewState, navigateUp: () -> Unit, viewModel: HTSViewModel) {
    val toolbarName = (state.htsManagerState as? WorkingState)?.let {
        (it.result as? DeviceHolder)?.deviceName()
    }

    if (toolbarName == null) {
        BackIconAppBar(stringResource(id = R.string.hts_title), navigateUp)
    } else {
        LoggerIconAppBar(toolbarName, navigateUp, { viewModel.onEvent(DisconnectEvent) }) {
            viewModel.onEvent(OpenLoggerEvent)
        }
    }
}

