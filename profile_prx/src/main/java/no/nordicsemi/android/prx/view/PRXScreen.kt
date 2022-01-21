package no.nordicsemi.android.prx.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.prx.R
import no.nordicsemi.android.prx.viewmodel.PRXViewModel
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.theme.view.DeviceConnectingView
import no.nordicsemi.android.utils.exhaustive

@Composable
fun PRXScreen() {
    val viewModel: PRXViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        BackIconAppBar(stringResource(id = R.string.prx_title)) {
            viewModel.onEvent(DisconnectEvent)
        }

        when (state) {
            is DisplayDataState -> ContentView(state.data) { viewModel.onEvent(it) }
            LoadingState -> DeviceConnectingView()
        }.exhaustive
    }
}
