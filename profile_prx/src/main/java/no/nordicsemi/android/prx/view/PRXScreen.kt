package no.nordicsemi.android.prx.view

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.prx.R
import no.nordicsemi.android.prx.data.PRXData
import no.nordicsemi.android.prx.service.PRXService
import no.nordicsemi.android.prx.viewmodel.PRXViewModel
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.utils.isServiceRunning

@Composable
fun PRXScreen(finishAction: () -> Unit) {
    val viewModel: PRXViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value
    val isActive = viewModel.isActive.collectAsState().value

    val context = LocalContext.current
    LaunchedEffect(isActive) {
        if (!isActive) {
            finishAction()
        }
        if (context.isServiceRunning(PRXService::class.java.name)) {
            val intent = Intent(context, PRXService::class.java)
            context.stopService(intent)
        }
    }

    LaunchedEffect("start-service") {
        if (!context.isServiceRunning(PRXService::class.java.name)) {
            val intent = Intent(context, PRXService::class.java)
            context.startService(intent)
        }
    }

    PRXView(state) { viewModel.onEvent(it) }
}

@Composable
private fun PRXView(state: PRXData, onEvent: (PRXScreenViewEvent) -> Unit) {
    Column {
        BackIconAppBar(stringResource(id = R.string.prx_title)) {
            onEvent(DisconnectEvent)
        }

        ContentView(state) { onEvent(it) }
    }
}

@Preview
@Composable
private fun PRXViewPreview(state: PRXData, onEvent: (PRXScreenViewEvent) -> Unit) {
    PRXView(state) { }
}
