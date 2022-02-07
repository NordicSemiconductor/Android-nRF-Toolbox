package no.nordicsemi.dfu.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.dfu.R
import no.nordicsemi.dfu.viewmodel.DFUViewModel

@Composable
fun DFUScreen() {
    val viewModel: DFUViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    Column {
        BackIconAppBar(stringResource(id = R.string.dfu_title)) {
            viewModel.onEvent(OnDisconnectButtonClick)
        }

//        when (state) {
//            is DisplayDataState -> DFUContentView(state.data) { viewModel.onEvent(it) }
//            LoadingState -> DeviceConnectingView()
//        }.exhaustive
    }
}
