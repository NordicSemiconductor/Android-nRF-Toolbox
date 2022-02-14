package no.nordicsemi.android.prx.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.prx.R
import no.nordicsemi.android.prx.viewmodel.PRXViewModel
import no.nordicsemi.android.service.*
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.theme.view.scanner.DeviceConnectingView
import no.nordicsemi.android.theme.view.scanner.DeviceDisconnectedView
import no.nordicsemi.android.theme.view.scanner.NoDeviceView
import no.nordicsemi.android.theme.view.scanner.Reason
import no.nordicsemi.android.utils.exhaustive

@Composable
fun PRXScreen() {
    val viewModel: PRXViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val navigateUp = { viewModel.onEvent(NavigateUpEvent) }

        BackIconAppBar(stringResource(id = R.string.prx_title), navigateUp)

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            when (state) {
                NoDeviceState -> NoDeviceView()
                is WorkingState -> when (state.result) {
                    is ConnectingResult,
                    is ReadyResult -> DeviceConnectingView { viewModel.onEvent(DisconnectEvent) }
                    is DisconnectedResult -> DeviceDisconnectedView(Reason.USER, navigateUp)
                    is LinkLossResult -> DeviceOutOfRangeView { viewModel.onEvent(DisconnectEvent) }
                    is MissingServiceResult -> DeviceDisconnectedView(Reason.MISSING_SERVICE, navigateUp)
                    is SuccessResult -> ContentView(state.result.data) { viewModel.onEvent(it) }
                }
            }.exhaustive
        }
    }
}
