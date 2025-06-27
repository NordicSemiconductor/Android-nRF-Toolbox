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
import no.nordicsemi.android.toolbox.lib.utils.Profile
import no.nordicsemi.android.toolbox.profile.data.BPSServiceData
import no.nordicsemi.android.toolbox.profile.data.BatteryServiceData
import no.nordicsemi.android.toolbox.profile.data.CGMServiceData
import no.nordicsemi.android.toolbox.profile.data.CSCServiceData
import no.nordicsemi.android.toolbox.profile.data.ChannelSoundingServiceData
import no.nordicsemi.android.toolbox.profile.data.DFSServiceData
import no.nordicsemi.android.toolbox.profile.data.GLSServiceData
import no.nordicsemi.android.toolbox.profile.data.HRSServiceData
import no.nordicsemi.android.toolbox.profile.data.HTSServiceData
import no.nordicsemi.android.toolbox.profile.data.LBSServiceData
import no.nordicsemi.android.toolbox.profile.data.ProfileServiceData
import no.nordicsemi.android.toolbox.profile.data.RSCSServiceData
import no.nordicsemi.android.toolbox.profile.data.ThroughputServiceData
import no.nordicsemi.android.toolbox.profile.data.UARTServiceData
import no.nordicsemi.android.toolbox.profile.data.toReason
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
import no.nordicsemi.android.toolbox.profile.view.lbs.BlinkyScreen
import no.nordicsemi.android.toolbox.profile.view.rscs.RSCSScreen
import no.nordicsemi.android.toolbox.profile.view.throughput.ThroughputScreen
import no.nordicsemi.android.toolbox.profile.view.uart.UARTScreen
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionState
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceData
import no.nordicsemi.android.toolbox.profile.viewmodel.DisconnectEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.NavigateUp
import no.nordicsemi.android.toolbox.profile.viewmodel.OnRetryClicked
import no.nordicsemi.android.toolbox.profile.viewmodel.OpenLoggerEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.ProfileUiEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.ProfileViewModel
import no.nordicsemi.android.ui.view.internal.DeviceConnectingView
import no.nordicsemi.android.ui.view.internal.DeviceDisconnectedView
import no.nordicsemi.android.ui.view.internal.DisconnectReason
import no.nordicsemi.android.ui.view.internal.LoadingView
import no.nordicsemi.android.ui.view.internal.ServiceDiscoveryView

@Composable
internal fun ProfileScreen() {
    val profileVM: ProfileViewModel = hiltViewModel()
    val deviceDataState by profileVM.deviceData.collectAsStateWithLifecycle()
    val deviceAddress = profileVM.address
    val onClickEvent: (ProfileUiEvent) -> Unit = { profileVM.onClickEvent(it) }

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
    onClickEvent: (ProfileUiEvent) -> Unit
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
                disconnectedReason = toReason(reason.reason),
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
    onClickEvent: (ProfileUiEvent) -> Unit,
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
                                    serviceData = serviceData as BPSServiceData,
                                )

                                Profile.CSC -> CSCScreen(
                                    serviceData = serviceData as CSCServiceData,
                                    onClickEvent = onClickEvent
                                )

                                Profile.CGM -> CGMScreen(
                                    serviceData = serviceData as CGMServiceData,
                                    onClickEvent = onClickEvent
                                )

                                Profile.DFS -> DFSScreen(
                                    serviceData = serviceData as DFSServiceData,
                                    onClick = onClickEvent
                                )

                                Profile.GLS -> GLSScreen(
                                    glsServiceData = serviceData as GLSServiceData,
                                    onClickEvent = onClickEvent
                                )

                                Profile.HRS -> HRSScreen(
                                    hrsServiceData = serviceData as HRSServiceData,
                                    onClickEvent = onClickEvent
                                )

                                Profile.HTS -> HTSScreen(
                                    htsServiceData = serviceData as HTSServiceData,
                                    onClickEvent = onClickEvent
                                )

                                Profile.RSCS -> RSCSScreen(
                                    serviceData = serviceData as RSCSServiceData,
                                    onClickEvent = onClickEvent
                                )

                                Profile.THROUGHPUT -> ThroughputScreen(
                                    serviceData = serviceData as ThroughputServiceData,
                                    onClickEvent = onClickEvent
                                )

                                Profile.BATTERY -> {
                                    // Battery level will be added at the end.
                                    // Do nothing here.
                                }

                                Profile.UART -> {
                                    UARTScreen(
                                        state = serviceData as UARTServiceData,
                                        onEvent = onClickEvent
                                    )
                                }

                                Profile.CHANNEL_SOUNDING -> {
                                    ChannelSoundingScreen(state = serviceData as ChannelSoundingServiceData)
                                }

                                Profile.LBS -> {
                                    BlinkyScreen(
                                        serviceData = serviceData as LBSServiceData,
                                        onClickEvent = onClickEvent
                                    )
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
