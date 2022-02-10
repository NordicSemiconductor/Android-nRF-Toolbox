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
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.theme.view.scanner.DeviceConnectingView
import no.nordicsemi.android.theme.view.scanner.DeviceDisconnectedView
import no.nordicsemi.android.theme.view.scanner.NoDeviceView
import no.nordicsemi.android.theme.view.scanner.Reason
import no.nordicsemi.android.utils.exhaustive

@Composable
fun BPSScreen() {
    val viewModel: BPSViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    Column {
        BackIconAppBar(stringResource(id = R.string.bps_title)) {
            viewModel.onEvent(DisconnectEvent)
        }

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            when (state) {
                NoDeviceState -> NoDeviceView()
                is WorkingState -> when (state.result) {
                    is ConnectingResult -> DeviceConnectingView()
                    is DisconnectedResult -> DeviceDisconnectedView(Reason.USER)
                    is LinkLossResult -> DeviceDisconnectedView(Reason.LINK_LOSS)
                    is MissingServiceResult -> DeviceDisconnectedView(Reason.MISSING_SERVICE)
                    is ReadyResult -> DeviceConnectingView()
                    is SuccessResult -> BPSContentView(state.result.data) { viewModel.onEvent(it) }
                }
            }.exhaustive
        }
    }
}
