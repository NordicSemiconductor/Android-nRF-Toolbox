package no.nordicsemi.android.csc.view

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.csc.R
import no.nordicsemi.android.csc.data.CSCData
import no.nordicsemi.android.csc.repository.CSCService
import no.nordicsemi.android.csc.viewmodel.CSCViewModel
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.utils.isServiceRunning

@Composable
fun CSCScreen(finishAction: () -> Unit) {
    val viewModel: CSCViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value
    val isScreenActive = viewModel.isActive.collectAsState().value

    val context = LocalContext.current
    LaunchedEffect(isScreenActive) {
        if (!isScreenActive) {
            finishAction()
        }
        if (context.isServiceRunning(CSCService::class.java.name)) {
            val intent = Intent(context, CSCService::class.java)
            context.stopService(intent)
        }
    }

    LaunchedEffect("start-service") {
        if (!context.isServiceRunning(CSCService::class.java.name)) {
            val intent = Intent(context, CSCService::class.java)
            context.startService(intent)
        }
    }

    CSCView(state) { viewModel.onEvent(it) }
}

@Composable
private fun CSCView(state: CSCData, onEvent: (CSCViewEvent) -> Unit) {
    Column {
        BackIconAppBar(stringResource(id = R.string.csc_title)) {
            onEvent(OnDisconnectButtonClick)
        }

        CSCContentView(state) { onEvent(it) }
    }
}
