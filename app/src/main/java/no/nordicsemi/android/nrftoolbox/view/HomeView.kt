package no.nordicsemi.android.nrftoolbox.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SocialDistance
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.common.analytics.view.AnalyticsPermissionButton
import no.nordicsemi.android.common.ui.view.NordicAppBar
import no.nordicsemi.android.nrftoolbox.R
import no.nordicsemi.android.nrftoolbox.viewmodel.HomeViewModel
import no.nordicsemi.android.nrftoolbox.viewmodel.UiEvent
import no.nordicsemi.android.toolbox.lib.utils.Profile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeView() {
    val viewModel = hiltViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onEvent: (UiEvent) -> Unit = { viewModel.onClickEvent(it) }

    Scaffold(
        topBar = {
            NordicAppBar(
                title = {
                    Text(stringResource(id = R.string.app_name))
                },
                actions = {
                    AnalyticsPermissionButton()
                }
            )
         },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEvent(UiEvent.OnConnectDeviceClick) },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Connect to device",
                    )
                    Text(text = stringResource(R.string.connect_device))
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                )
                .padding(horizontal = 16.dp)
                .consumeWindowInsets(paddingValues),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                // Show the title at the top
                SectionTitle(
                    title = stringResource(R.string.connected_devices)
                )
            }
            item {
                if (state.connectedDevices.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        state.connectedDevices.keys.forEach { device ->
                            state.connectedDevices[device]?.let { deviceData ->
                                if (deviceData.connectionState.isConnected) {
                                    // Skip if no services
                                    if (deviceData.services.isEmpty()) return@forEach
                                    // Case 1: If only one service, show it directly like battery service
                                    if (deviceData.services.size == 1 && deviceData.services.first().profile == Profile.BATTERY) {
                                        FeatureButton(
                                            iconId = R.drawable.ic_battery,
                                            description = R.string.battery_module_full,
                                            deviceName = deviceData.peripheral.name,
                                            deviceAddress = deviceData.peripheral.address,
                                            onClick = {
                                                onEvent(
                                                    UiEvent.OnDeviceClick(
                                                        deviceData.peripheral.address,
                                                        deviceData.services.first().profile
                                                    )
                                                )
                                            },
                                        )
                                    }
                                    // Case 2: Show the first *non-Battery* profile.
                                    // This ensures only one service is shown per peripheral when multiple services are available.
                                    deviceData.services.firstOrNull { it.profile != Profile.BATTERY }
                                        ?.let { serviceManager ->
                                            val peripheral = deviceData.peripheral
                                            val services = deviceData.services
                                            when (serviceManager.profile) {
                                                Profile.HRS -> FeatureButton(
                                                    iconId = R.drawable.ic_hrs,
                                                    description = R.string.hrs_module_full,
                                                    deviceName = peripheral.name,
                                                    profileNames = services.map { it.profile.toString() },
                                                    deviceAddress = peripheral.address,
                                                    onClick = {
                                                        onEvent(
                                                            UiEvent.OnDeviceClick(
                                                                peripheral.address,
                                                                serviceManager.profile
                                                            )
                                                        )
                                                    },
                                                )

                                                Profile.HTS -> FeatureButton(
                                                    iconId = R.drawable.ic_hts,
                                                    description = R.string.hts_module_full,
                                                    deviceName = peripheral.name,
                                                    deviceAddress = peripheral.address,
                                                    profileNames = services.map { it.profile.toString() },
                                                    onClick = {
                                                        onEvent(
                                                            UiEvent.OnDeviceClick(
                                                                peripheral.address,
                                                                serviceManager.profile
                                                            )
                                                        )
                                                    },
                                                )

                                                Profile.BPS -> FeatureButton(
                                                    iconId = R.drawable.ic_bps,
                                                    description = R.string.bps_module_full,
                                                    deviceName = peripheral.name,
                                                    deviceAddress = peripheral.address,
                                                    profileNames = services.map { it.profile.toString() },
                                                    onClick = {
                                                        onEvent(
                                                            UiEvent.OnDeviceClick(
                                                                peripheral.address,
                                                                serviceManager.profile
                                                            )
                                                        )
                                                    },
                                                )

                                                Profile.GLS -> FeatureButton(
                                                    iconId = R.drawable.ic_gls,
                                                    description = R.string.gls_module_full,
                                                    deviceName = peripheral.name,
                                                    deviceAddress = peripheral.address,
                                                    profileNames = services.map { it.profile.toString() },
                                                    onClick = {
                                                        onEvent(
                                                            UiEvent.OnDeviceClick(
                                                                peripheral.address,
                                                                serviceManager.profile
                                                            )
                                                        )
                                                    },
                                                )

                                                Profile.CGM -> FeatureButton(
                                                    iconId = R.drawable.ic_cgm,
                                                    description = R.string.cgm_module_full,
                                                    deviceName = peripheral.name,
                                                    deviceAddress = peripheral.address,
                                                    profileNames = services.map { it.profile.toString() },
                                                    onClick = {
                                                        onEvent(
                                                            UiEvent.OnDeviceClick(
                                                                peripheral.address,
                                                                serviceManager.profile
                                                            )
                                                        )
                                                    },
                                                )

                                                Profile.RSCS -> FeatureButton(
                                                    iconId = R.drawable.ic_rscs,
                                                    description = R.string.rscs_module_full,
                                                    deviceName = peripheral.name,
                                                    deviceAddress = peripheral.address,
                                                    profileNames = services.map { it.profile.toString() },
                                                    onClick = {
                                                        onEvent(
                                                            UiEvent.OnDeviceClick(
                                                                peripheral.address,
                                                                serviceManager.profile
                                                            )
                                                        )
                                                    },
                                                )

                                                Profile.DFS -> FeatureButton(
                                                    iconId = R.drawable.ic_distance,
                                                    description = R.string.direction_module_full,
                                                    deviceName = peripheral.name,
                                                    deviceAddress = peripheral.address,
                                                    profileNames = services.map { it.profile.toString() },
                                                    onClick = {
                                                        onEvent(
                                                            UiEvent.OnDeviceClick(
                                                                peripheral.address,
                                                                serviceManager.profile
                                                            )
                                                        )
                                                    },
                                                )

                                                Profile.CSC -> FeatureButton(
                                                    iconId = R.drawable.ic_csc,
                                                    description = R.string.csc_module_full,
                                                    deviceName = peripheral.name,
                                                    deviceAddress = peripheral.address,
                                                    profileNames = services.map { it.profile.toString() },
                                                    onClick = {
                                                        onEvent(
                                                            UiEvent.OnDeviceClick(
                                                                peripheral.address,
                                                                serviceManager.profile
                                                            )
                                                        )
                                                    },
                                                )

                                                Profile.THROUGHPUT -> {
                                                    FeatureButton(
                                                        iconId = Icons.Default.SyncAlt,
                                                        description = R.string.throughput_module,
                                                        deviceName = peripheral.name,
                                                        deviceAddress = peripheral.address,
                                                        profileNames = services.map { it.profile.toString() },
                                                        onClick = {
                                                            onEvent(
                                                                UiEvent.OnDeviceClick(
                                                                    peripheral.address,
                                                                    serviceManager.profile
                                                                )
                                                            )
                                                        },
                                                    )
                                                }

                                                Profile.UART -> {
                                                    FeatureButton(
                                                        iconId = R.drawable.ic_uart,
                                                        description = R.string.uart_module_full,
                                                        deviceName = peripheral.name,
                                                        deviceAddress = peripheral.address,
                                                        profileNames = services.map { it.profile.toString() },
                                                        onClick = {
                                                            onEvent(
                                                                UiEvent.OnDeviceClick(
                                                                    peripheral.address,
                                                                    serviceManager.profile
                                                                )
                                                            )
                                                        },
                                                    )
                                                }

                                                Profile.CHANNEL_SOUNDING -> {
                                                    FeatureButton(
                                                        iconId = Icons.Default.SocialDistance,
                                                        description = R.string.channel_sounding_module,
                                                        deviceName = peripheral.name,
                                                        deviceAddress = peripheral.address,
                                                        profileNames = services.map { it.profile.toString() },
                                                        onClick = {
                                                            onEvent(
                                                                UiEvent.OnDeviceClick(
                                                                    peripheral.address,
                                                                    serviceManager.profile
                                                                )
                                                            )
                                                        },
                                                    )
                                                }

                                                Profile.LBS -> {
                                                    FeatureButton(
                                                        iconId = Icons.Default.Lightbulb,
                                                        description = R.string.lbs_blinky_module,
                                                        deviceName = peripheral.name,
                                                        deviceAddress = peripheral.address,
                                                        profileNames = services.map { it.profile.toString() },
                                                        onClick = {
                                                            onEvent(
                                                                UiEvent.OnDeviceClick(
                                                                    peripheral.address,
                                                                    serviceManager.profile
                                                                )
                                                            )
                                                        },
                                                    )
                                                }

                                                Profile.DFU -> {
                                                    FeatureButton(
                                                        iconId = R.drawable.ic_dfu,
                                                        description = R.string.dfu_module_full,
                                                        deviceName = peripheral.name,
                                                        deviceAddress = peripheral.address,
                                                        profileNames = services.map { it.profile.toString() },
                                                        onClick = {
                                                            onEvent(
                                                                UiEvent.OnDeviceClick(
                                                                    peripheral.address,
                                                                    serviceManager.profile
                                                                )
                                                            )
                                                        },
                                                    )
                                                }

                                                Profile.BATTERY -> {
                                                    // Battery service is handled above, do nothing here.
                                                }
                                            }
                                        }
                                }

                            }
                        }
                    }
                } else {
                    NoConnectedDeviceView()
                }
            }
            item {
                SectionTitle(
                    title = stringResource(R.string.links)
                )
            }
            item {
                Links { onEvent(it) }
            }
        }
    }
}
