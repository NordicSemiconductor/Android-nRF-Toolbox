package no.nordicsemi.android.cgms.view

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.cgms.R
import no.nordicsemi.android.cgms.data.CGMData
import no.nordicsemi.android.cgms.repository.CGMService
import no.nordicsemi.android.cgms.viewmodel.CGMScreenViewModel
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.utils.isServiceRunning

@Composable
fun CGMScreen(finishAction: () -> Unit) {
    val viewModel: CGMScreenViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value
    val isScreenActive = viewModel.isActive.collectAsState().value

    val context = LocalContext.current
    LaunchedEffect(isScreenActive) {
        if (!isScreenActive) {
            finishAction()
        }
        if (context.isServiceRunning(CGMService::class.java.name)) {
            val intent = Intent(context, CGMService::class.java)
            context.stopService(intent)
        }
    }

    LaunchedEffect("start-service") {
        if (!context.isServiceRunning(CGMService::class.java.name)) {
            val intent = Intent(context, CGMService::class.java)
            context.startService(intent)
        }
    }

    CGMView(state) {
        viewModel.onEvent(it)
    }
}

@Composable
private fun CGMView(state: CGMData, onEvent: (CGMViewEvent) -> Unit) {
    Column {
        BackIconAppBar(stringResource(id = R.string.cgms_title)) {
            onEvent(DisconnectEvent)
        }

        CGMContentView(state, onEvent)
    }
}

