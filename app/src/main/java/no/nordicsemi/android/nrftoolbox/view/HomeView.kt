package no.nordicsemi.android.nrftoolbox.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import no.nordicsemi.android.common.permissions.notification.RequestNotificationPermission
import no.nordicsemi.android.nrftoolbox.R
import no.nordicsemi.android.nrftoolbox.viewmodel.HomeViewEvent
import no.nordicsemi.android.nrftoolbox.viewmodel.HomeViewModel
import no.nordicsemi.android.toolbox.libs.core.Profile

private const val DFU_PACKAGE_NAME = "no.nordicsemi.android.dfu"
private const val DFU_LINK =
    "https://play.google.com/store/apps/details?id=no.nordicsemi.android.dfu"

private const val LOGGER_PACKAGE_NAME = "no.nordicsemi.android.log"

@Composable
internal fun HomeView() {
    val viewModel = hiltViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onEvent: (HomeViewEvent) -> Unit = { viewModel.onClickEvent(it) }

    Scaffold(
        topBar = { TitleAppBar(stringResource(id = R.string.app_name), false) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { onEvent(HomeViewEvent.OnConnectDeviceClick) }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add device from scanner"
                    )
                    Text(text = "Connect device")
                }
            }
        }
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            RequireBluetooth {
                RequestNotificationPermission {
                    if (state.connectedDevices.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Connected devices",
                                style = MaterialTheme.typography.titleLarge,
                            )
                            state.connectedDevices.values.forEach { (peripheral, services) ->
                                services.forEach { serviceManager ->
                                    when (serviceManager.profile) {
                                        Profile.HRS -> FeatureButton(
                                            iconId = R.drawable.ic_hrs,
                                            profileName = R.string.hrs_module_full,
                                            deviceName = peripheral.name,
                                            true
                                        ) { onEvent(HomeViewEvent.OnDeviceClick(peripheral.address)) }

                                        Profile.HTS -> FeatureButton(
                                            iconId = R.drawable.ic_hts,
                                            profileName = R.string.hts_module_full,
                                            deviceName = peripheral.name,
                                            true
                                        ) { onEvent(HomeViewEvent.OnDeviceClick(peripheral.address)) }

                                        Profile.BPS -> FeatureButton(
                                            iconId = R.drawable.ic_bps,
                                            profileName = R.string.bps_module_full,
                                            deviceName = peripheral.name,
                                            true
                                        ) { onEvent(HomeViewEvent.OnDeviceClick(peripheral.address)) }

                                        Profile.GLS -> FeatureButton(
                                            iconId = R.drawable.ic_gls,
                                            profileName = R.string.gls_module_full,
                                            deviceName = peripheral.name,
                                            true
                                        ) { onEvent(HomeViewEvent.OnDeviceClick(peripheral.address)) }

                                        Profile.CGM -> FeatureButton(
                                            iconId = R.drawable.ic_cgm,
                                            profileName = R.string.cgm_module_full,
                                            deviceName = peripheral.name,
                                            true
                                        ) { onEvent(HomeViewEvent.OnDeviceClick(peripheral.address)) }

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
                }
            }
        }
    }
}
