package no.nordicsemi.android.hts.view

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
import no.nordicsemi.android.hts.R
import no.nordicsemi.android.hts.data.HTSData
import no.nordicsemi.android.hts.service.HTSService
import no.nordicsemi.android.hts.viewmodel.HTSViewModel
import no.nordicsemi.android.utils.isServiceRunning

@Composable
fun HTSScreen(finishAction: () -> Unit) {
    val viewModel: HTSViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    val context = LocalContext.current
    LaunchedEffect(state.isScreenActive) {
        if (!state.isScreenActive) {
            finishAction()
        }
        if (context.isServiceRunning(HTSService::class.java.name)) {
            val intent = Intent(context, HTSService::class.java)
            context.stopService(intent)
        }
    }

    LaunchedEffect("start-service") {
        if (!context.isServiceRunning(HTSService::class.java.name)) {
            val intent = Intent(context, HTSService::class.java)
            context.startService(intent)
        }
    }

    HRSView(state) { viewModel.onEvent(it) }
}

@Composable
private fun HRSView(state: HTSData, onEvent: (HTSScreenViewEvent) -> Unit) {
    Column {
        TopAppBar(title = { Text(text = stringResource(id = R.string.hts_title)) })

        HTSContentView(state) { onEvent(it) }
    }
}
