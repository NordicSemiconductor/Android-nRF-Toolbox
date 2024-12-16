package no.nordicsemi.android.toolbox.libs.profile

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.common.permissions.ble.RequireBluetooth
import no.nordicsemi.android.service.profile.CustomReason
import no.nordicsemi.android.service.profile.DeviceDisconnectionReason
import no.nordicsemi.android.service.profile.StateReason
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.toolbox.libs.core.data.BPSServiceData
import no.nordicsemi.android.toolbox.libs.core.data.BatteryServiceData
import no.nordicsemi.android.toolbox.libs.core.data.CGMServiceData
import no.nordicsemi.android.toolbox.libs.core.data.CSCServiceData
import no.nordicsemi.android.toolbox.libs.core.data.DFSServiceData
import no.nordicsemi.android.toolbox.libs.core.data.GLSServiceData
import no.nordicsemi.android.toolbox.libs.core.data.HRSServiceData
import no.nordicsemi.android.toolbox.libs.core.data.HTSServiceData
import no.nordicsemi.android.toolbox.libs.core.data.ProfileServiceData
import no.nordicsemi.android.toolbox.libs.core.data.RSCSServiceData
import no.nordicsemi.android.toolbox.libs.profile.view.BPSScreen
import no.nordicsemi.android.toolbox.libs.profile.view.CGMScreen
import no.nordicsemi.android.toolbox.libs.profile.view.CSCScreen
import no.nordicsemi.android.toolbox.libs.profile.view.DFSScreen
import no.nordicsemi.android.toolbox.libs.profile.view.GLSScreen
import no.nordicsemi.android.toolbox.libs.profile.view.HRSScreen
import no.nordicsemi.android.toolbox.libs.profile.view.HTSScreen
import no.nordicsemi.android.toolbox.libs.profile.view.RSCSScreen
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionState
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewModel
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceData
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DisconnectEvent
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.NavigateUp
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.OnRetryClicked
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.OpenLoggerEvent
import no.nordicsemi.android.toolbox.libs.profile.view.BatteryLevelView
import no.nordicsemi.android.ui.view.ProfileAppBar
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
                    .verticalScroll(rememberScrollState())
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

                    DeviceConnectionState.Idle -> LoadingView()
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
                            Text(text = "Cancel")
                        }
                    }
                } else
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    deviceData.serviceData.forEach { serviceData ->
                        when (serviceData.profile) {
                            Profile.BPS -> BPSScreen(serviceData as BPSServiceData)
                            Profile.CSC -> CSCScreen(serviceData as CSCServiceData, onClickEvent)
                            Profile.CGM -> CGMScreen(
                                serviceData = serviceData as CGMServiceData
                            ) { onClickEvent(it) }

                            Profile.DFS -> DFSScreen(
                                serviceData as DFSServiceData
                            ) { onClickEvent(it) }

                            Profile.GLS -> GLSScreen(
                                device = peripheral.name ?: peripheral.address,
                                glsServiceData = serviceData as GLSServiceData,
                            ) { onClickEvent(it) }

                            Profile.HRS -> HRSScreen(serviceData as HRSServiceData, onClickEvent)
                            Profile.HTS -> HTSScreen(serviceData as HTSServiceData, onClickEvent)

                            Profile.RSCS -> RSCSScreen(serviceData as RSCSServiceData, onClickEvent)

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
