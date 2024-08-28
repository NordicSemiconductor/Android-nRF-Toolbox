package no.nordicsemi.android.toolbox.scanner.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.permissions.ble.RequireBluetooth
import no.nordicsemi.android.common.permissions.ble.RequireLocation
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.android.common.ui.view.CircularIcon
import no.nordicsemi.android.common.ui.view.NordicAppBar
import no.nordicsemi.android.toolbox.scanner.repository.ScanningState
import no.nordicsemi.android.toolbox.scanner.viewmodel.ScannerViewModel
import no.nordicsemi.kotlin.ble.client.android.Peripheral

@Composable
internal fun ScannerScreen() {
    val viewModel: ScannerViewModel = hiltViewModel()
    val isDeviceSelected = rememberSaveable { mutableStateOf(false) }

    if (isDeviceSelected.value) {
        // Show device connectionState
        viewModel.selectedDevice?.let { DeviceConnectionScreen() }

    } else {
        ScannerView(isDeviceSelected)
    }

}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ScannerView(isDeviceSelected: MutableState<Boolean>) {
    val viewModel: ScannerViewModel = hiltViewModel()
    val pullToRefreshState = rememberPullToRefreshState()
    val scanningState by viewModel.scanningState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Handle back button press
    BackHandler {
        viewModel.navigateBack()
    }
    Scaffold(
        topBar = {
            ScannerAppBar(
                { Text(text = "Scanner") },
                showProgress = true,
                onNavigationButtonClick = { viewModel.navigateBack() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            RequireBluetooth {
                RequireLocation { isLocationRequiredAndDisabled ->

                    Column(modifier = Modifier.fillMaxSize()) {
                        PullToRefreshBox(
                            isRefreshing = scanningState is ScanningState.Loading,
                            onRefresh = {
                                viewModel.refreshScanning()
                                scope.launch {
                                    pullToRefreshState.animateToHidden()
                                }
                            },
                            state = pullToRefreshState,
                            content = {
                                DeviceListView(
                                    isLocationRequiredAndDisabled = isLocationRequiredAndDisabled,
                                    bleState = scanningState,
                                    modifier = Modifier.fillMaxSize(),
                                    onClick = {
                                        isDeviceSelected.value = true
                                        viewModel.onDeviceSelected(it)
                                    },
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun DeviceListView(
    isLocationRequiredAndDisabled: Boolean,
    bleState: ScanningState,
    modifier: Modifier,
    onClick: (Peripheral) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        when (bleState) {
            is ScanningState.DevicesDiscovered -> {
                if (bleState.devices.isEmpty()) {
                    item { ScanEmptyView(isLocationRequiredAndDisabled) }
                } else {
                    DeviceListItems(bleState.devices, onClick)
                }
            }

            is ScanningState.Error -> item { ScanErrorView(bleState.error) }
            ScanningState.Loading -> item { ScanEmptyView(isLocationRequiredAndDisabled) }
        }
    }

}

@Suppress("FunctionName")
internal fun LazyListScope.DeviceListItems(
    devices: List<Peripheral>,
    onClick: (Peripheral) -> Unit,
) {
    // Filter out peripherals with null names.
    val nonNullPeripherals = devices.filter { it.name != null }.toList()
    items(nonNullPeripherals.size) { index ->
        Box(modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick(nonNullPeripherals[index]) }
            .padding(8.dp)
        ) {
            DeviceListItem(
                name = nonNullPeripherals[index].name,
                address = nonNullPeripherals[index].address
            )
        }
    }
}

@Composable
private fun DeviceListItem(
    name: String?,
    address: String,
) {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically
    )
    {
        CircularIcon(Icons.Default.Bluetooth)

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = name ?: "Unknown device",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview
@Composable
private fun DeviceListItemPreview() {
    NordicTheme {
        DeviceListItem(
            name = "Device name",
            address = "AA:BB:CC:DD:EE:FF"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScannerAppBar(
    title: @Composable () -> Unit,
    showProgress: Boolean = false,
    backButtonIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    onNavigationButtonClick: (() -> Unit)? = null,
) {
    NordicAppBar(
        title = title,
        backButtonIcon = backButtonIcon,
        onNavigationButtonClick = onNavigationButtonClick,
        actions = {
            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .size(30.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
    )
}
