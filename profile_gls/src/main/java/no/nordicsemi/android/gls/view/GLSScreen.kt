package no.nordicsemi.android.gls.view

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.gls.R
import no.nordicsemi.android.gls.viewmodel.DisconnectEvent
import no.nordicsemi.android.gls.viewmodel.GLSViewModel
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.theme.view.DeviceConnectingView
import no.nordicsemi.android.utils.exhaustive

@Composable
fun GLSScreen() {
    val viewModel: GLSViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    Log.d("AAATESTAAA", "$viewModel") //TODO fix screen rotation

    Column {
        BackIconAppBar(stringResource(id = R.string.gls_title)) {
            viewModel.onEvent(DisconnectEvent)
        }

        when (state) {
            is DisplayDataState -> GLSContentView(state.data) { viewModel.onEvent(it) }
            LoadingState -> DeviceConnectingView()
        }.exhaustive
    }
}
