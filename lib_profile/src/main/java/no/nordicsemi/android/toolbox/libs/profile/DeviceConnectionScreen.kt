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
import no.nordicsemi.android.toolbox.libs.profile.view.HRSScreen
import no.nordicsemi.android.toolbox.libs.profile.view.HTSScreen
import no.nordicsemi.android.toolbox.libs.profile.view.LoadingView
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.BatteryServiceData
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewModel
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceData
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DisconnectEvent
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.HRSServiceData
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.HTSServiceData
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.NavigateUp
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.OnRetryClicked
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.OpenLoggerEvent
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.ProfileServiceData
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
                when (val connectionState = deviceData.connectionState) {
                    ConnectionState.Connected -> DeviceConnectedView(deviceData, onClickEvent)
                    ConnectionState.Disconnecting -> LoadingView()
                    is ConnectionState.Disconnected -> ReconnectDevice(
                        reason = connectionState.reason,
                        deviceAddress = deviceAddress,
                        onClickEvent
                    )

                    ConnectionState.Closed -> {
                        ReconnectDevice(deviceData.disconnectionReason, deviceAddress, onClickEvent)
                    }

                    else -> DeviceConnectingView(
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReconnectDevice(
    reason: ConnectionState.Disconnected.Reason?,
    deviceAddress: String,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    DeviceDisconnectedView(
        reason = reason ?: ConnectionState.Disconnected.Reason.Unknown(0),
        modifier = Modifier.padding(16.dp)
    ) {
        Button(
            onClick = { onClickEvent(OnRetryClicked(deviceAddress)) },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Reconnect")
        }
    }
}

@Composable
private fun DeviceConnectedView(
    deviceData: DeviceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit,
) {
    deviceData.peripheral?.let {
        when {
            deviceData.isMissingServices -> {
                DeviceDisconnectedView(
                    reason = DisconnectReason.MISSING_SERVICE,
                    modifier = Modifier.padding(16.dp)
                )
            }

            deviceData.serviceData.isEmpty() -> {
                DeviceConnectingView(modifier = Modifier.padding(16.dp))
            }

            else -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    deviceData.serviceData.forEach { serviceData ->
                        when (serviceData) {
                            is HRSServiceData -> HRSScreen(serviceData, onClickEvent)
                            is HTSServiceData -> HTSScreen(serviceData, onClickEvent)
                            is BatteryServiceData -> {
                                // Battery level will be added at the end.
                                // Do nothing here.
                            }
                        }
                    }
                    // Battery level will be added at the end.
                    DisplayBatteryLevel(deviceData.serviceData)
                }
            }

        }
    }
}

@Composable
private fun DisplayBatteryLevel(serviceData: List<ProfileServiceData>) {
    serviceData
        .filterIsInstance<BatteryServiceData>()
        .firstOrNull { it.batteryLevel != null }
        ?.batteryLevel?.let { BatteryLevelView(it) }
}
