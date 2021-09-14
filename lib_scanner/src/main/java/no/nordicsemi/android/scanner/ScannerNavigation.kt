package no.nordicsemi.android.scanner

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import no.nordicsemi.android.events.exhaustive
import no.nordicsemi.android.scanner.bluetooth.BluetoothNotAvailableScreen
import no.nordicsemi.android.scanner.bluetooth.BluetoothNotEnabledScreen
import no.nordicsemi.android.scanner.permissions.RequestPermissionScreen

@Composable
fun ScannerRoute(navController: NavController) {
    val viewModel = hiltViewModel<NordicBleScannerViewModel>()

    val scannerStatus = viewModel.state.collectAsState().value.scannerStatus

    Column {
        TopAppBar(title = { Text(text = stringResource(id = R.string.scanner__devices_list)) })
        ScannerScreen(navController, scannerStatus) { viewModel.onEvent(it) }
    }
}

@Composable
private fun ScannerScreen(
    navController: NavController,
    scannerStatus: ScannerStatus,
    onEvent: (ScannerViewEvent) -> Unit
) {
    when (scannerStatus) {
        ScannerStatus.PERMISSION_REQUIRED -> RequestPermissionScreen { onEvent(ScannerViewEvent.PERMISSION_CHECKED) }
        ScannerStatus.NOT_AVAILABLE -> BluetoothNotAvailableScreen()
        ScannerStatus.DISABLED -> BluetoothNotEnabledScreen { onEvent(ScannerViewEvent.BLUETOOTH_ENABLED) }
        ScannerStatus.ENABLED -> {
            onEvent(ScannerViewEvent.ENABLE_SCANNING)
            ListOfDevicesScreen {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("result", it)
                navController.popBackStack()
                onEvent(ScannerViewEvent.DISABLE_SCANNING)
            }
        }
    }.exhaustive
}
