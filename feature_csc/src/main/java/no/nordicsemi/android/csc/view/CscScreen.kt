package no.nordicsemi.android.csc.view

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.csc.R
import no.nordicsemi.android.csc.service.CSCService
import no.nordicsemi.android.csc.viewmodel.CSCViewState
import no.nordicsemi.android.csc.viewmodel.CscViewModel
import no.nordicsemi.android.utils.isServiceRunning

@Composable
fun CscScreen(finishAction: () -> Unit) {
    val viewModel: CscViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    val context = LocalContext.current
    LaunchedEffect(state.isScreenActive) {
        if (!state.isScreenActive) {
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
private fun CSCView(state: CSCViewState, onEvent: (CSCViewEvent) -> Unit) {
    Column {
        TopAppBar(title = { Text(text = stringResource(id = R.string.csc_title)) })

        ContentView(state) { onEvent(it) }
    }
}
