package no.nordicsemi.android.hrs.view

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.hrs.R
import no.nordicsemi.android.hrs.data.HRSData
import no.nordicsemi.android.hrs.service.HRSService
import no.nordicsemi.android.hrs.viewmodel.HRSViewModel
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.utils.isServiceRunning

@Composable
fun HRSScreen(finishAction: () -> Unit) {
    val viewModel: HRSViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value
    val isActive = viewModel.isActive.collectAsState().value

    val context = LocalContext.current
    LaunchedEffect(isActive) {
        if (!isActive) {
            finishAction()
        }
        if (context.isServiceRunning(HRSService::class.java.name)) {
            val intent = Intent(context, HRSService::class.java)
            context.stopService(intent)
        }
    }

    LaunchedEffect("start-service") {
        if (!context.isServiceRunning(HRSService::class.java.name)) {
            val intent = Intent(context, HRSService::class.java)
            context.startService(intent)
        }
    }

    HRSView(state) { viewModel.onEvent(it) }
}

@Composable
private fun HRSView(state: HRSData, onEvent: (HRSScreenViewEvent) -> Unit) {
    Column {
        BackIconAppBar(stringResource(id = R.string.hrs_title)) {
            onEvent(DisconnectEvent)
        }

        HRSContentView(state) { onEvent(it) }
    }
}
