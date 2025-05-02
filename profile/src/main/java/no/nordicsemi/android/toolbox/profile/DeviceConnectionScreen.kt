package no.nordicsemi.android.toolbox.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.common.permissions.ble.RequireBluetooth
import no.nordicsemi.android.common.permissions.ble.RequireLocation
import no.nordicsemi.android.common.permissions.notification.RequestNotificationPermission
import no.nordicsemi.android.service.profile.CustomReason
import no.nordicsemi.android.service.profile.DeviceDisconnectionReason
import no.nordicsemi.android.service.profile.StateReason
import no.nordicsemi.android.toolbox.profile.data.BPSServiceData
import no.nordicsemi.android.toolbox.profile.data.BatteryServiceData
import no.nordicsemi.android.toolbox.profile.data.CGMServiceData
import no.nordicsemi.android.toolbox.profile.data.CSCServiceData
import no.nordicsemi.android.toolbox.profile.data.ChannelSoundingServiceData
import no.nordicsemi.android.toolbox.profile.data.DFSServiceData
import no.nordicsemi.android.toolbox.profile.data.GLSServiceData
import no.nordicsemi.android.toolbox.profile.data.HRSServiceData
import no.nordicsemi.android.toolbox.profile.data.HTSServiceData
import no.nordicsemi.android.toolbox.profile.data.Profile
import no.nordicsemi.android.toolbox.profile.data.ProfileServiceData
import no.nordicsemi.android.toolbox.profile.data.RSCSServiceData
import no.nordicsemi.android.toolbox.profile.data.ThroughputServiceData
import no.nordicsemi.android.toolbox.profile.data.UARTServiceData
import no.nordicsemi.android.toolbox.profile.view.battery.BatteryLevelView
import no.nordicsemi.android.toolbox.profile.view.bps.BPSScreen
import no.nordicsemi.android.toolbox.profile.view.cgms.CGMScreen
import no.nordicsemi.android.toolbox.profile.view.channelSounding.ChannelSoundingScreen
import no.nordicsemi.android.toolbox.profile.view.cscs.CSCScreen
import no.nordicsemi.android.toolbox.profile.view.directionFinder.DFSScreen
import no.nordicsemi.android.toolbox.profile.view.gls.GLSScreen
import no.nordicsemi.android.toolbox.profile.view.hrs.HRSScreen
import no.nordicsemi.android.toolbox.profile.view.hts.HTSScreen
import no.nordicsemi.android.toolbox.profile.view.internal.ProfileAppBar
import no.nordicsemi.android.toolbox.profile.view.rscs.RSCSScreen
import no.nordicsemi.android.toolbox.profile.view.throughput.ThroughputScreen
import no.nordicsemi.android.toolbox.profile.view.uart.UARTScreen
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionState
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewModel
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceData
import no.nordicsemi.android.toolbox.profile.viewmodel.DisconnectEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.NavigateUp
import no.nordicsemi.android.toolbox.profile.viewmodel.OnRetryClicked
import no.nordicsemi.android.toolbox.profile.viewmodel.OpenLoggerEvent
import no.nordicsemi.android.ui.view.internal.DeviceConnectingView
import no.nordicsemi.android.ui.view.internal.DeviceDisconnectedView
import no.nordicsemi.android.ui.view.internal.DisconnectReason
import no.nordicsemi.android.ui.view.internal.LoadingView
import no.nordicsemi.android.ui.view.internal.ServiceDiscoveryView

@Composable
internal fun DeviceConnectionScreen() {
    val deviceConnectionViewModel: DeviceConnectionViewModel = hiltViewModel()
    val deviceDataState by deviceConnectionViewModel.deviceData.collectAsStateWithLifecycle()
    val deviceAddress = deviceConnectionViewModel.address
    val onClickEvent: (DeviceConnectionViewEvent) -> Unit =
        { deviceConnectionViewModel.onClickEvent(it) }

    Scaffold(
        topBar = {
            ProfileAppBar(
                deviceName = when (val state = deviceDataState) {
                    is DeviceConnectionState.Connected -> state.data.peripheral?.name
                        ?: deviceAddress

                    is DeviceConnectionState.Disconnected -> state.device?.name ?: deviceAddress

                    else -> deviceAddress
                },
                title = deviceAddress,
                connectionState = deviceDataState,
                navigateUp = { onClickEvent(NavigateUp) },
                disconnect = { onClickEvent(DisconnectEvent(deviceAddress)) },
                openLogger = { onClickEvent(OpenLoggerEvent) }
            )
        },
    ) { paddingValues ->
        RequireBluetooth {
            RequireLocation {
                RequestNotificationPermission {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .fillMaxSize()
                            .padding(paddingValues),
                        horizontalAlignment = Alignment.CenterHorizontally,
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

                            DeviceConnectionState.Idle -> LoadingView()
                        }
                    }
                }
            }
        }
    }
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
                    Text(text = stringResource(id = R.string.reconnect))
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
                    Text(text = stringResource(id = R.string.reconnect))
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
    deviceData.peripheral?.let { peripheral ->
        when {
            deviceData.isMissingServices -> {
                DeviceDisconnectedView(
                    reason = DisconnectReason.MISSING_SERVICE,
                    modifier = Modifier.padding(16.dp)
                )
            }

            else -> {
                if (deviceData.serviceData.isEmpty()) {
                    ServiceDiscoveryView(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Button(
                            onClick = { onClickEvent(DisconnectEvent(peripheral.address)) },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(text = stringResource(id = R.string.cancel))
                        }
                    }
                } else
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        deviceData.serviceData.forEach { serviceData ->
                            when (serviceData.profile) {
                                Profile.BPS -> BPSScreen(
                                    serviceData = serviceData as BPSServiceData
                                )

                                Profile.CSC -> CSCScreen(
                                    serviceData = serviceData as CSCServiceData,
                                ) { onClickEvent(it) }

                                Profile.CGM -> CGMScreen(
                                    serviceData = serviceData as CGMServiceData
                                ) { onClickEvent(it) }

                                Profile.DFS -> DFSScreen(
                                    serviceData = serviceData as DFSServiceData
                                ) { onClickEvent(it) }

                                Profile.GLS -> GLSScreen(
                                    glsServiceData = serviceData as GLSServiceData,
                                ) { onClickEvent(it) }

                                Profile.HRS -> HRSScreen(
                                    hrsServiceData = serviceData as HRSServiceData,
                                ) { onClickEvent(it) }

                                Profile.HTS -> HTSScreen(
                                    htsServiceData = serviceData as HTSServiceData,
                                ) { onClickEvent(it) }

                                Profile.RSCS -> RSCSScreen(
                                    serviceData = serviceData as RSCSServiceData,
                                ) { onClickEvent(it) }

                                Profile.THROUGHPUT -> ThroughputScreen(
                                    serviceData = serviceData as ThroughputServiceData,
                                ) { onClickEvent(it) }

                                Profile.BATTERY -> {
                                    // Battery level will be added at the end.
                                    // Do nothing here.
                                }

                                Profile.UART -> {
                                    UARTScreen(
                                        state = serviceData as UARTServiceData,
                                    ) { onClickEvent(it) }
                                }

                                Profile.CHANNEL_SOUNDING -> {
                                    ChannelSoundingScreen(state = serviceData as ChannelSoundingServiceData)
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
