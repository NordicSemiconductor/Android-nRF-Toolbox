package no.nordicsemi.android.toolbox.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
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
import no.nordicsemi.android.toolbox.lib.utils.Profile
import no.nordicsemi.android.toolbox.profile.data.displayMessage
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
import no.nordicsemi.android.toolbox.profile.viewmodel.ProfileUiState
import no.nordicsemi.android.toolbox.profile.viewmodel.ProfileViewModel
import no.nordicsemi.android.ui.view.internal.DeviceConnectingView
import no.nordicsemi.android.ui.view.internal.DeviceDisconnectedView
import no.nordicsemi.android.ui.view.internal.DisconnectReason
import no.nordicsemi.android.ui.view.internal.ServiceDiscoveryView

@Composable
internal fun ProfileScreen() {
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val deviceAddress = profileViewModel.address

    // Event handler now sends simpler, context-free events.
    val onEvent: (ConnectionEvent) -> Unit = { event ->
        profileViewModel.onEvent(event)
    }
    // Handle back press to navigate up.
    BackHandler {
        onEvent(ConnectionEvent.NavigateUp)
    }

    Scaffold(
        contentWindowInsets = WindowInsets.displayCutout
            .only(WindowInsetsSides.Horizontal),
        topBar = {
            // The device name is derived directly from the current state.
            val deviceName = (uiState as? ProfileUiState.Connected)
                ?.deviceData?.peripheral?.name
                ?: deviceAddress

            ProfileAppBar(
                deviceName = deviceName,
                title = deviceAddress,
                // The AppBar needs to be updated to accept the new ProfileUiState
                connectionState = uiState,
                navigateUp = { onEvent(ConnectionEvent.NavigateUp) },
                disconnect = { onEvent(ConnectionEvent.DisconnectEvent) },
                openLogger = { onEvent(ConnectionEvent.OpenLoggerEvent) }
            )
        },
    ) { paddingValues ->
        RequireBluetooth {
            RequireLocation {
                RequestNotificationPermission {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .imePadding(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // The main content switches based on the UI state.
                        when (val state = uiState) {
                            is ProfileUiState.Connected -> DeviceConnectedView(
                                state = state,
                                onEvent = onEvent
                            )

                            is ProfileUiState.Disconnected -> {
                                if (state.reason == null) {
                                    // This is the initial state before connection attempt
                                    // show device connecting view instead.
                                    DeviceConnectingView(
                                        modifier = Modifier.padding(16.dp)
                                    )
                                } else {
                                    DeviceDisconnectedView(
                                        disconnectedReason = state.reason.displayMessage(),
                                        isMissingService = false,
                                        modifier = Modifier.padding(16.dp),
                                    ) {
                                        Button(
                                            modifier = Modifier.padding(16.dp),
                                            onClick = { onEvent(ConnectionEvent.OnRetryClicked) },

                                            ) {
                                            Text(text = stringResource(id = R.string.reconnect))
                                        }
                                    }
                                }
                            }

                            ProfileUiState.Loading -> DeviceConnectingView(
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun DeviceConnectedView(
    state: ProfileUiState.Connected,
    onEvent: (ConnectionEvent) -> Unit,
) {
    // Check for missing services directly from the state object.
    if (state.isMissingServices) {
        DeviceDisconnectedView(
            reason = DisconnectReason.MISSING_SERVICE,
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(16.dp)
            .imePadding()
    ) {
        // Show service discovery view if services are not yet available.
        if (state.deviceData.services.isEmpty()) {
            ServiceDiscoveryView(modifier = Modifier) {
                Button(
                    onClick = { onEvent(ConnectionEvent.DisconnectEvent) },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        } else {
            // Iterate through the available service managers.
            state.deviceData.services.forEach { serviceManager ->
                Column(modifier = Modifier.imePadding()) {
                    val needsMaxValueLength = serviceManager.profile in listOf(
                        Profile.CHANNEL_SOUNDING, Profile.UART, Profile.THROUGHPUT
                    )

                    // Request max value length if needed and not already set.
                    if (needsMaxValueLength) {
                        LaunchedEffect(Unit) {
                            if (state.maxValueLength == null) {
                                onEvent(ConnectionEvent.RequestMaxValueLength)
                            }
                        }
                    }

                    // Display the appropriate screen for each profile.
                    when (serviceManager.profile) {
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
                        Profile.BATTERY -> BatteryScreen()
                        Profile.THROUGHPUT -> ThroughputScreen(state.maxValueLength)
                        Profile.UART -> UARTScreen(state.maxValueLength)
                    }
                }
            }
        }
    }
}