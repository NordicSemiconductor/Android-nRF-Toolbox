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
import no.nordicsemi.android.rscs.viewmodel.RSCSViewModel
import no.nordicsemi.android.service.*
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.theme.view.scanner.DeviceConnectingView
import no.nordicsemi.android.theme.view.scanner.DeviceDisconnectedView
import no.nordicsemi.android.theme.view.scanner.NoDeviceView
import no.nordicsemi.android.theme.view.scanner.Reason
import no.nordicsemi.android.utils.exhaustive

@Composable
fun RSCSScreen() {
    val viewModel: RSCSViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    Column {
        val navigateUp = { viewModel.onEvent(NavigateUpEvent) }

        BackIconAppBar(stringResource(id = R.string.rscs_title)) {
            viewModel.onEvent(DisconnectEvent)
        }

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            when (state) {
                NoDeviceState -> NoDeviceView()
                is WorkingState -> when (state.result) {
                    is ConnectingResult,
                    is ReadyResult
                    -> DeviceConnectingView { viewModel.onEvent(DisconnectEvent) }
                    is DisconnectedResult -> DeviceDisconnectedView(Reason.USER, navigateUp)
                    is LinkLossResult -> DeviceDisconnectedView(Reason.LINK_LOSS, navigateUp)
                    is MissingServiceResult -> DeviceDisconnectedView(Reason.MISSING_SERVICE, navigateUp)
                    is SuccessResult -> RSCSContentView(state.result.data) { viewModel.onEvent(it) }
                }
            }.exhaustive
        }
    }
}
