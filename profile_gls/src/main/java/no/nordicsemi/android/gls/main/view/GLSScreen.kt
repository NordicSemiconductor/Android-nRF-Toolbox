package no.nordicsemi.android.gls.main.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.gls.R
import no.nordicsemi.android.gls.main.viewmodel.GLSViewModel
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.theme.view.DeviceConnectingView
import no.nordicsemi.android.utils.exhaustive

@Composable
fun GLSScreen() {
    val viewModel: GLSViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    Column {
        BackIconAppBar(stringResource(id = R.string.gls_title)) {
            viewModel.onEvent(DisconnectEvent)
        }

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            when (state) {
                is DisplayDataState -> GLSContentView(state.data) { viewModel.onEvent(it) }
                LoadingState -> DeviceConnectingView()
            }.exhaustive
        }
    }
}
