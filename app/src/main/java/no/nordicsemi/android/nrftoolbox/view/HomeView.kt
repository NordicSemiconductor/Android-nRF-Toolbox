package no.nordicsemi.android.nrftoolbox.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SocialDistance
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.nrftoolbox.R
import no.nordicsemi.android.nrftoolbox.viewmodel.HomeViewModel
import no.nordicsemi.android.nrftoolbox.viewmodel.UiEvent
import no.nordicsemi.android.toolbox.lib.utils.Profile

@Composable
internal fun HomeView() {
    val viewModel = hiltViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onEvent: (UiEvent) -> Unit = { viewModel.onClickEvent(it) }

    Scaffold(
        topBar = { TitleAppBar(stringResource(id = R.string.app_name)) },
        contentWindowInsets = WindowInsets.displayCutout
            .only(WindowInsetsSides.Horizontal),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEvent(UiEvent.OnConnectDeviceClick) },
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add device from scanner"
                    )
                    Text(text = stringResource(R.string.connect_device))
                }
            }
        }
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            Text(
                text = stringResource(R.string.connected_devices),
                modifier = Modifier
                    .alpha(0.5f)
                    .padding(start = 16.dp),
            )
            if (state.connectedDevices.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    state.connectedDevices.values.forEach { (peripheral, services) ->
                        // Skip if no services
                        if (services.isEmpty()) return@forEach
                        // Case 1: If only one service, show it directly like battery service
                        if (services.size == 1 && services.first().profile == Profile.BATTERY) {
                            FeatureButton(
                                iconId = R.drawable.ic_battery,
                                profileName = R.string.battery_module_full,
                                deviceName = peripheral.name,
                                onClick = {
                                    onEvent(
                                        UiEvent.OnDeviceClick(
                                            peripheral.address,
                                            services.first().profile
                                        )
                                    )
                                },
                            )
                        }
                        // Case 2: Show the first *non-Battery* profile.
                        // This ensures only one service is shown per peripheral when multiple services are available.
                        services.firstOrNull { it.profile != Profile.BATTERY }
                            ?.let { serviceManager ->
                                when (serviceManager.profile) {
                                    Profile.HRS -> FeatureButton(
                                        iconId = R.drawable.ic_hrs,
                                        profileName = R.string.hrs_module_full,
                                        deviceName = peripheral.name,
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
                                        profileName = R.string.hts_module_full,
                                        deviceName = peripheral.name,
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
                                        profileName = R.string.bps_module_full,
                                        deviceName = peripheral.name,
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
                                        profileName = R.string.gls_module_full,
                                        deviceName = peripheral.name,
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
                                        profileName = R.string.cgm_module_full,
                                        deviceName = peripheral.name,
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
                                        profileName = R.string.rscs_module_full,
                                        deviceName = peripheral.name,
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
                                        profileName = R.string.direction_module_full,
                                        deviceName = peripheral.name,
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
                                        profileName = R.string.csc_module_full,
                                        deviceName = peripheral.name,
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
                                            profileName = R.string.throughput_module,
                                            deviceName = peripheral.name,
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
                                            profileName = R.string.uart_module_full,
                                            deviceName = peripheral.name,
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
                                            profileName = R.string.channel_sounding_module,
                                            deviceName = peripheral.name,
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
                                            profileName = R.string.lbs_blinky_module,
                                            deviceName = peripheral.name,
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

                                    else -> {
                                        // TODO: Add more profiles
                                    }
                                }
                            }
                    }
                }
            } else {
                NoConnectedDeviceView()
            }

            Links { onEvent(it) }
        }
    }
}
