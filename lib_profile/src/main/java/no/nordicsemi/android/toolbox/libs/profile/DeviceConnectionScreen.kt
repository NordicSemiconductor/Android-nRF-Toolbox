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
import no.nordicsemi.android.toolbox.libs.profile.data.Profile
import no.nordicsemi.android.toolbox.libs.profile.data.service.BPSServiceData
import no.nordicsemi.android.toolbox.libs.profile.data.service.BatteryServiceData
import no.nordicsemi.android.toolbox.libs.profile.data.service.HRSServiceData
import no.nordicsemi.android.toolbox.libs.profile.data.service.HTSServiceData
import no.nordicsemi.android.toolbox.libs.profile.data.service.ProfileServiceData
import no.nordicsemi.android.toolbox.libs.profile.service.CustomReason
import no.nordicsemi.android.toolbox.libs.profile.service.DeviceDisconnectionReason
import no.nordicsemi.android.toolbox.libs.profile.service.StateReason
import no.nordicsemi.android.toolbox.libs.profile.view.BPSScreen
import no.nordicsemi.android.toolbox.libs.profile.view.HRSScreen
import no.nordicsemi.android.toolbox.libs.profile.view.HTSScreen
import no.nordicsemi.android.toolbox.libs.profile.view.LoadingView
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionState
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
    val deviceDataState by deviceConnectionViewModel.deviceData.collectAsStateWithLifecycle()
    val onClickEvent: (DeviceConnectionViewEvent) -> Unit =
        { deviceConnectionViewModel.onClickEvent(it) }

    LaunchedEffect(deviceAddress) {
        deviceConnectionViewModel.connectToPeripheral(deviceAddress)
    }

    Scaffold(
        topBar = {
            ProfileAppBar(
                deviceName = when (val state = deviceDataState) {
                    is DeviceConnectionState.Connected -> state.data.peripheral?.name
                        ?: deviceAddress

                    else -> deviceAddress
                },
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
                when (val state = deviceDataState) {
                    is DeviceConnectionState.Connected -> {
                        DeviceConnectedView(
                            state.data,
                            onClickEvent
                        )
                    }

                    DeviceConnectionState.Connecting -> DeviceConnectingView(
                        modifier = Modifier.padding(16.dp)
                    )

                    is DeviceConnectionState.Disconnected -> {

                        state.reason?.let {
                            DeviceDisconnectedView(
                                it,
                                deviceAddress,
                                onClickEvent
                            )
                        }
                    }

                    is DeviceConnectionState.Error -> ErrorView(state.message, onClickEvent)
                    DeviceConnectionState.Idle -> LoadingView()
                }
            }
        }
    }
}

@Composable
fun ErrorView(message: String, onClickEvent: (DeviceConnectionViewEvent) -> Unit) {
    TODO("Not yet implemented")
}

@Composable
private fun DeviceDisconnectedView(
    reason: DeviceDisconnectionReason,
    deviceAddress: String,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    when (reason) {
        is CustomReason -> {
            DeviceDisconnectedView(
                reason = reason.reason,
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

        is StateReason -> {
            DeviceDisconnectedView(
                reason = reason.reason,
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
                        when (serviceData.profile) {
                            Profile.BPS -> BPSScreen(serviceData as BPSServiceData)
                            Profile.HTS -> HTSScreen(serviceData as HTSServiceData, onClickEvent)
                            Profile.HRS -> HRSScreen(serviceData as HRSServiceData, onClickEvent)
                            Profile.BATTERY -> {
                                // Battery level will be added at the end.
                                // Do nothing here.
                            }

                            else -> TODO()
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
