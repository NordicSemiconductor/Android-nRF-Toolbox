package no.nordicsemi.android.csc.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.csc.R
import no.nordicsemi.android.csc.viewmodel.CSCViewModel
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.theme.view.DeviceConnectingView
import no.nordicsemi.android.utils.exhaustive

@Composable
fun CSCScreen() {
    val viewModel: CSCViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    Column {
        BackIconAppBar(stringResource(id = R.string.csc_title)) {
            viewModel.onEvent(OnDisconnectButtonClick)
        }

        when (state) {
            is DisplayDataState -> CSCContentView(state.data) { viewModel.onEvent(it) }
            LoadingState -> DeviceConnectingView()
        }.exhaustive
    }
}
