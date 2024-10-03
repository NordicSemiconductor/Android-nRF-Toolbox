package no.nordicsemi.android.toolbox.libs.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import no.nordicsemi.android.common.permissions.ble.RequireBluetooth
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.profile.data.hrs.HRSServiceData
import no.nordicsemi.android.toolbox.libs.profile.data.hts.HTSServiceData
import no.nordicsemi.android.toolbox.libs.profile.view.HRSScreen
import no.nordicsemi.android.toolbox.libs.profile.view.HTSScreen
import no.nordicsemi.android.toolbox.libs.profile.view.LoadingView
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewModel
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceData
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DisconnectEvent
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.NavigateUp
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.OnRetryClicked
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.OpenLoggerEvent
import no.nordicsemi.android.ui.view.BatteryLevelView
import no.nordicsemi.android.ui.view.ProfileAppBar
import no.nordicsemi.android.ui.view.internal.DeviceConnectingView
import no.nordicsemi.android.ui.view.internal.DeviceDisconnectedView
import no.nordicsemi.android.ui.view.internal.DisconnectReason
import no.nordicsemi.kotlin.ble.core.ConnectionState

@Composable
internal fun DeviceConnectionScreen(deviceAddress: String) {
    val deviceConnectionViewModel: DeviceConnectionViewModel = hiltViewModel()
    val deviceData by deviceConnectionViewModel.deviceData.collectAsStateWithLifecycle()
    val onClickEvent: (DeviceConnectionViewEvent) -> Unit =
        { deviceConnectionViewModel.onClickEvent(it) }

    LaunchedEffect(deviceAddress) {
        deviceConnectionViewModel.connectToPeripheral(deviceAddress)
    }

    Scaffold(
        topBar = {
            ProfileAppBar(
                deviceName = deviceData.peripheral?.name ?: deviceAddress,
                connectionState = deviceData.connectionState,
                title = R.string.hts_title,
                navigateUp = { onClickEvent(NavigateUp) },
                disconnect = { onClickEvent(DisconnectEvent(deviceAddress)) },
                openLogger = { onClickEvent(OpenLoggerEvent) }
            )
        },
    ) { paddingValues ->
        RequireBluetooth {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                when (val s = deviceData.connectionState) {
                    ConnectionState.Connecting -> DeviceConnectingView(
                        modifier = Modifier.padding(16.dp)
                    )

                    ConnectionState.Connected -> DeviceConnectedView(deviceData, onClickEvent)
                    ConnectionState.Disconnecting -> LoadingView()
                    is ConnectionState.Disconnected -> {
                        s.reason?.let {
                            DeviceDisconnectedView(
                                reason = it,
                                content = { paddingValues ->
                                    Button(
                                        modifier = Modifier.padding(paddingValues),
                                        onClick = { onClickEvent(OnRetryClicked(deviceAddress)) },
                                    ) {
                                        Text(text = "Reconnect")
                                    }
                                }
                            )
                        }
                    }

                    null -> {
                        Text("Connecting to $deviceAddress")
                    }
                }
            }
        }
    }
}

@Composable
internal fun DeviceConnectedView(
    deviceData: DeviceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit,
) {
    if (deviceData.peripheral != null) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(16.dp)
        ) {
            when (deviceData.isMissingServices) {
                true -> {
                    DeviceDisconnectedView(
                        reason = DisconnectReason.MISSING_SERVICE,
                    )
                }

                false -> {
                    deviceData.htsServiceData.takeIf { it != HTSServiceData() }
                        ?.let { htsServiceData ->
                            HTSScreen(
                                htsServiceData = htsServiceData,
                            ) { onClickEvent(it) }
                        }
                    deviceData.hrsServiceData.takeIf { it != HRSServiceData() }
                        ?.let { htsServiceData ->
                            HRSScreen(
                                hrsServiceData = htsServiceData,
                            ) { onClickEvent(it) }
                        }

                    if (deviceData.batteryLevel != null) {
                        BatteryLevelView(deviceData.batteryLevel)
                    }
                }
            }
        }
    }
}
