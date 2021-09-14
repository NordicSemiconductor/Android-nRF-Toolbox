package no.nordicsemi.android.csc.view

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import no.nordicsemi.android.csc.R
import no.nordicsemi.android.csc.service.CSCService
import no.nordicsemi.android.events.exhaustive

@Composable
internal fun CscScreen(navController: NavController, viewModel: CscViewModel = hiltViewModel()) {

    val secondScreenResult = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<BluetoothDevice>("result")?.observeAsState()

    secondScreenResult?.value?.let {
        viewModel.onEvent(OnBluetoothDeviceSelected(it))

        val intent = Intent(LocalContext.current, CSCService::class.java).apply {
            putExtra("no.nordicsemi.android.nrftoolbox.EXTRA_DEVICE_ADDRESS", it.address)
        }
        LocalContext.current.startService(intent)

        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.set("result", null)
    }

    val state = viewModel.state.collectAsState().value

    CSCView(navController, state) { viewModel.onEvent(it) }
}

@Composable
private fun CSCView(navController: NavController, state: CSCViewState, onEvent: (CSCViewEvent) -> Unit) {
    Column {
        TopAppBar(title = { Text(text = stringResource(id = R.string.csc_title)) })

        when (state) {
            is CSCViewConnectedState -> ConnectedView(state) { onEvent(it) }
            is CSCViewNotConnectedState -> NotConnectedScreen(navController, state) {
                onEvent(it)
            }
        }.exhaustive
    }
}

@Composable
private fun NotConnectedScreen(
    navController: NavController,
    state: CSCViewNotConnectedState,
    onEvent: (CSCViewEvent) -> Unit
) {
    if (state.showScannerDialog) {
        navController.navigate("scanner-destination")
        onEvent(OnMovedToScannerScreen)
    }

    NotConnectedView(onEvent)
}

@Composable
private fun NotConnectedView(
    onEvent: (CSCViewEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(id = R.string.csc_no_connection))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onEvent(OnConnectButtonClick) }) {
            Text(text = stringResource(id = R.string.csc_connect))
        }
    }
}

@Composable
private fun ConnectedView(state: CSCViewConnectedState, onEvent: (CSCViewEvent) -> Unit) {
    if (state.showDialog) {
        SelectWheelSizeDialog { onEvent(it) }
    }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WheelSizeView(state, onEvent)

        SpeedUnitRadioGroup(state.selectedSpeedUnit) { onEvent(it) }

        SensorsReadingView(state = state)

        Button(onClick = { onEvent(OnDisconnectButtonClick) }) {
            Text(text = stringResource(id = R.string.csc_disconnect))
        }

    }
}

@Preview
@Composable
private fun NotConnectedPreview() {
    NotConnectedView { }
}

@Preview
@Composable
private fun ConnectedPreview() {
    ConnectedView(CSCViewConnectedState()) { }
}
