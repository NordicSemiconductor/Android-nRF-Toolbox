package no.nordicsemi.android.toolbox.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.toolbox.libs.profile.data.hts.data.HTSServiceData
import no.nordicsemi.android.toolbox.scanner.changed.ClientData
import no.nordicsemi.android.toolbox.scanner.changed.ClientViewModel
import no.nordicsemi.android.toolbox.scanner.view.hts.view.DisconnectEvent
import no.nordicsemi.android.toolbox.scanner.view.hts.view.HTSScreen
import no.nordicsemi.android.toolbox.scanner.view.hts.view.LoadingView
import no.nordicsemi.android.toolbox.scanner.view.hts.view.NavigateUp
import no.nordicsemi.android.toolbox.scanner.view.hts.view.OnRetryClicked
import no.nordicsemi.android.toolbox.scanner.view.hts.view.ProfileScreenViewEvent
import no.nordicsemi.android.toolbox.scanner.view.hts.view.RequestNotificationPermission
import no.nordicsemi.android.ui.view.BatteryLevelView
import no.nordicsemi.android.ui.view.ProfileAppBar
import no.nordicsemi.android.ui.view.internal.DeviceDisconnectedView
import no.nordicsemi.android.ui.view.internal.DisconnectReason
import no.nordicsemi.kotlin.ble.core.ConnectionState

val ConnectDeviceDestinationId = createDestination<String, Unit>("connect-device-destination")
val ConnectDeviceDestination = defineDestination(ConnectDeviceDestinationId) {
    ConnectDeviceScreen()
}

@Composable
internal fun ConnectDeviceScreen() {
    val clientViewModel: ClientViewModel = hiltViewModel()
    val clientData by clientViewModel.clientData.collectAsStateWithLifecycle()
    val onClickEvent: (ProfileScreenViewEvent) -> Unit = { clientViewModel.onClickEvent(it) }
    val peripheral = clientViewModel.address

    Scaffold(
        topBar = {
            ProfileAppBar(
                deviceName = clientData.peripheral?.name ?: peripheral,
                connectionState = clientData.connectionState,
                title = R.string.hts_title,
                navigateUp = { onClickEvent(NavigateUp) },
                disconnect = { onClickEvent(DisconnectEvent(peripheral)) },
                openLogger = { }
            )
        },
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (val s = clientData.connectionState) {
                ConnectionState.Connected -> {
                    ConnectedView(clientData, onClickEvent)
                }

                ConnectionState.Connecting -> {
                    LoadingView()
                }

                is ConnectionState.Disconnected -> {
                    s.reason?.let {
                        DeviceDisconnectedView(
                            reason = it,
                            content = { paddingValues ->
                                Button(
                                    modifier = Modifier.padding(paddingValues),
                                    onClick = { onClickEvent(OnRetryClicked(peripheral)) },
                                ) {
                                    Text(text = "Reconnect")
                                }
                            }
                        )
                    }
                }

                ConnectionState.Disconnecting -> {
                    LoadingView()
                }

                null -> {
                    Text("Connecting to $peripheral")
                }
            }
        }
    }
}

@Composable
internal fun ConnectedView(
    clientData: ClientData,
    onClickEvent: (ProfileScreenViewEvent) -> Unit,
) {
    if (clientData.peripheral != null) {
        RequestNotificationPermission { granted ->
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(16.dp)
            ) {
                when (clientData.isMissingServices) {
                    true -> {
                        DeviceDisconnectedView(
                            reason = DisconnectReason.MISSING_SERVICE,
                            modifier = Modifier.padding(16.dp),
                        )
                    }

                    false -> {
                        clientData.htsServiceData.takeIf { it != HTSServiceData() }
                            ?.let { htsServiceData ->
                                HTSScreen(
                                    htsServiceData = htsServiceData,
                                ) { onClickEvent(it) }
                            }

                        if (clientData.batteryLevel != null) {
                            BatteryLevelView(clientData.batteryLevel)
                        }
                    }
                }
            }
        }
    }
}
