package no.nordicsemi.android.toolbox.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import no.nordicsemi.android.toolbox.profile.data.toReason
import no.nordicsemi.android.toolbox.profile.view.battery.BatteryScreen
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
import no.nordicsemi.android.toolbox.profile.viewmodel.ConnectionEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.ConnectionViewModel
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionState
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceData
import no.nordicsemi.android.ui.view.internal.DeviceConnectingView
import no.nordicsemi.android.ui.view.internal.DisconnectReason
import no.nordicsemi.android.ui.view.internal.LoadingView
import no.nordicsemi.android.ui.view.internal.ServiceDiscoveryView

@Composable
internal fun ProfileScreen() {
    val connectionViewModel: ConnectionViewModel = hiltViewModel()
    val deviceAddress = connectionViewModel.address
    val deviceDataState by connectionViewModel.deviceState.collectAsStateWithLifecycle()
    val onClickEvent: (ConnectionEvent) -> Unit = { event ->
        connectionViewModel.onConnectionEvent(event)
    }

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
                navigateUp = { onClickEvent(ConnectionEvent.NavigateUp) },
                disconnect = { onClickEvent(ConnectionEvent.DisconnectEvent(deviceAddress)) },
                openLogger = { onClickEvent(ConnectionEvent.OpenLoggerEvent) }
            )
        },
    ) { paddingValues ->
        // Get notch padding for devices with a display cutout (notch)
        val notchPadding = WindowInsets.displayCutout
            .only(WindowInsetsSides.Horizontal)
            .asPaddingValues()

        RequireBluetooth {
            RequireLocation {
                RequestNotificationPermission {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(notchPadding)
                            .imePadding(),
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
                                modifier = Modifier
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
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

                            DeviceConnectionState.Idle, DeviceConnectionState.Disconnecting -> LoadingView()
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun DeviceDisconnectedView(
    reason: DeviceDisconnectionReason,
    deviceAddress: String,
    onClickEvent: (ConnectionEvent) -> Unit
) {
    when (reason) {
        is CustomReason -> {
            no.nordicsemi.android.ui.view.internal.DeviceDisconnectedView(
                reason = reason.reason,
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Button(
                    onClick = { onClickEvent(ConnectionEvent.OnRetryClicked(deviceAddress)) },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = stringResource(id = R.string.reconnect))
                }
            }
        }

        is StateReason -> {
            no.nordicsemi.android.ui.view.internal.DeviceDisconnectedView(
                disconnectedReason = toReason(reason.reason),
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Button(
                    onClick = { onClickEvent(ConnectionEvent.OnRetryClicked(deviceAddress)) },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = stringResource(id = R.string.reconnect))
                }
            }
        }
    }
}

@Composable
internal fun DeviceConnectedView(
    deviceData: DeviceData,
    onClickEvent: (ConnectionEvent) -> Unit,
) {
    // Is missing services?
    deviceData.peripheral?.let { peripheral ->
        when {
            deviceData.isMissingServices -> {
                no.nordicsemi.android.ui.view.internal.DeviceDisconnectedView(
                    reason = DisconnectReason.MISSING_SERVICE,
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                )
            }

            else -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .padding(16.dp)
                        .imePadding()
                ) {
                    deviceData.peripheralProfileMap[deviceData.peripheral]?.forEach { profile ->
                        Column(
                            modifier = Modifier
                                .imePadding()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Requires max value length to be set.
                            LaunchedEffect(key1 = profile.profile == Profile.CHANNEL_SOUNDING || profile.profile == Profile.UART) {
                                if (deviceData.maxValueLength == null) {
                                    onClickEvent(ConnectionEvent.RequestMaxValueLength)
                                }
                            }
                            when (profile.profile) {
                                Profile.HTS -> HTSScreen()
                                Profile.CHANNEL_SOUNDING -> ChannelSoundingScreen()
                                Profile.BPS -> BPSScreen()
                                Profile.CSC -> CSCScreen()
                                Profile.CGM -> CGMScreen()
                                Profile.DFS -> DFSScreen()
                                Profile.GLS -> GLSScreen()
                                Profile.HRS -> HRSScreen()
                                Profile.LBS -> BlinkyScreen()
                                Profile.RSCS -> RSCSScreen()
                                Profile.THROUGHPUT -> ThroughputScreen(deviceData.maxValueLength)
                                Profile.UART -> UARTScreen(deviceData.maxValueLength)

                                else -> {
                                    // Do nothing.
                                }
                            }
                            if (profile.profile == Profile.BATTERY) {
                                // Battery level will be added at the end.
                                BatteryScreen()
                            }
                        }
                    } ?: run {
                        ServiceDiscoveryView(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                        ) {
                            Button(
                                onClick = {
                                    onClickEvent(
                                        ConnectionEvent.DisconnectEvent(
                                            peripheral.address
                                        )
                                    )
                                },
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(text = stringResource(id = R.string.cancel))
                            }
                        }
                    }
                }
            }
        }
    }
}