package no.nordicsemi.android.rscs.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.rscs.R
import no.nordicsemi.android.rscs.data.RSCSData
import no.nordicsemi.android.rscs.viewmodel.RSCSViewModel
import no.nordicsemi.android.service.*
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.theme.view.LoggerIconAppBar
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.ui.scanner.ui.DeviceConnectingView
import no.nordicsemi.ui.scanner.ui.DeviceDisconnectedView
import no.nordicsemi.ui.scanner.ui.NoDeviceView
import no.nordicsemi.ui.scanner.ui.Reason

@Composable
fun RSCSScreen() {
    val viewModel: RSCSViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    Column {
        val navigateUp = { viewModel.onEvent(NavigateUpEvent) }

        AppBar(state, navigateUp, viewModel)

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
                    is SuccessResult -> RSCSContentView(state.result.data) { viewModel.onEvent(it) }
                }
            }.exhaustive
        }
    }
}

@Composable
private fun AppBar(state: RSCSViewState, navigateUp: () -> Unit, viewModel: RSCSViewModel) {
    val toolbarName = (state as? WorkingState)?.let {
        (it.result as? SuccessResult<RSCSData>)?.deviceName()
    }

    if (toolbarName == null) {
        BackIconAppBar(stringResource(id = R.string.rscs_title), navigateUp)
    } else {
        LoggerIconAppBar(toolbarName, navigateUp, { viewModel.onEvent(DisconnectEvent) }) {
            viewModel.onEvent(OpenLoggerEvent)
        }
    }
}
