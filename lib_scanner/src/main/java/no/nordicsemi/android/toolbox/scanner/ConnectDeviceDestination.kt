package no.nordicsemi.android.toolbox.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.common.permissions.ble.RequireBluetooth
import no.nordicsemi.android.toolbox.scanner.changed.ClientData
import no.nordicsemi.android.toolbox.scanner.changed.ClientViewModel
import no.nordicsemi.android.toolbox.scanner.view.ScannerAppBar
import no.nordicsemi.android.toolbox.scanner.view.hts.view.HTSScreen
import no.nordicsemi.android.toolbox.scanner.view.hts.view.LoadingView
import no.nordicsemi.android.toolbox.scanner.view.hts.view.RequestNotificationPermission
import no.nordicsemi.android.ui.view.BatteryLevelView
import no.nordicsemi.kotlin.ble.core.ConnectionState

val ConnectDeviceDestinationId = createDestination<String, Unit>("connect-device-destination")
val ConnectDeviceDestination = defineDestination(ConnectDeviceDestinationId) {
    val simpleNavigationViewModel: SimpleNavigationViewModel = hiltViewModel()
    ConnectDeviceScreen(simpleNavigationViewModel.parameterOf(ConnectDeviceDestinationId))
}

@Composable
internal fun ConnectDeviceScreen(peripheral: String) {
    val clientViewModel: ClientViewModel = hiltViewModel()
    val clientData by clientViewModel.clientData.collectAsStateWithLifecycle()

    LaunchedEffect(peripheral) {
        clientViewModel.connectToPeripheral(peripheral)
    }

    Scaffold(
        topBar = {
            ScannerAppBar(title = { Text(clientData.peripheral?.name ?: peripheral) },
                onNavigationButtonClick = {
                    // Navigate back
                })
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (clientData.connectionState) {
                ConnectionState.Connected -> {
                    ConnectedView(clientData)
                }

                ConnectionState.Connecting -> {
                    LoadingView()
                }

                is ConnectionState.Disconnected -> TODO()
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
fun ConnectedView(clientData: ClientData) {
    if (clientData.peripheral != null) {
        RequireBluetooth {
            RequestNotificationPermission { granted ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    if (clientData.htsServiceData != null) {
                        HTSScreen(
                            htsServiceData = clientData.htsServiceData,
                        ) {

                        }
                    }
                    if (clientData.batteryLevel != null) {
                        BatteryLevelView(clientData.batteryLevel)
                    }
                }

            }
        }
    }
}
