package no.nordicsemi.android.bps.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.bps.R
import no.nordicsemi.android.bps.data.BPSData
import no.nordicsemi.android.bps.viewmodel.BPSViewModel
import no.nordicsemi.android.theme.view.BackIconAppBar

@Composable
fun BPSScreen(finishAction: () -> Unit) {
    val viewModel: BPSViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value
    val isScreenActive = viewModel.isActive.collectAsState().value

    BPSView(state) { viewModel.onEvent(it) }
}

@Composable
private fun BPSView(state: BPSData, onEvent: (BPSScreenViewEvent) -> Unit) {
    Column {
        BackIconAppBar(stringResource(id = R.string.bps_title)) {
            onEvent(DisconnectEvent)
        }

        BPSContentView(state) { onEvent(it) }
    }
}
