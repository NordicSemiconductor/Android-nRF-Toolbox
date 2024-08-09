package no.nordicsemi.android.nrftoolbox.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.common.permissions.ble.RequireBluetooth
import no.nordicsemi.android.common.permissions.ble.RequireLocation
import no.nordicsemi.android.nrftoolbox.R
import no.nordicsemi.android.nrftoolbox.viewmodel.HomeViewModel

@Composable
internal fun HomeView() {
    val viewModel = hiltViewModel<HomeViewModel>()
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TitleAppBar(stringResource(id = R.string.app_name), state.isScanning)
        }
    ) {
        RequireBluetooth {
            RequireLocation { isLocationRequiredAndDisabled ->
                // Both Bluetooth and Location permissions are granted.
                // We can now start scanning.
                Column(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    LaunchedEffect(key1 = isLocationRequiredAndDisabled) {
                        viewModel.startScanning()
                    }
                    state.devices.forEach { device ->
                        ScannedDeviceRow(device = device) {
                            viewModel.connect(device)
                        }
                    }
                }
            }
        }
    }
}
