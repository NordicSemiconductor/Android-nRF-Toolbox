package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.lib.profile.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.toolbox.libs.core.data.DFSServiceData
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.viewmodel.DFSViewEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.internal.EmptyView

@Composable
internal fun SectionBluetoothDeviceComponent(
    data: DFSServiceData,
    selectedDevice: PeripheralBluetoothAddress?,
    onEvent: (DeviceConnectionViewEvent) -> Unit
) {
    val devices = data.data.keys.toList()

    when {
        selectedDevice == null && devices.isNotEmpty() -> ScreenSection {
            NotSelectedView(devices) {
                onEvent(DFSViewEvent.OnBluetoothDeviceSelected(it))
            }
        }

        selectedDevice == null -> {
            EmptyView(
                R.string.device_no_devices,
                R.string.device_no_devices_hint,
            )
        }

        else -> {
            MeasuredDevices(selectedDevice, devices, onEvent)
        }
    }

}

@Composable
private fun MeasuredDevices(
    selectedDevice: PeripheralBluetoothAddress,
    devices: List<PeripheralBluetoothAddress>,
    onEvent: (DeviceConnectionViewEvent) -> Unit
) {
    var showDropdownMenu by rememberSaveable { mutableStateOf(false) }
    var width by rememberSaveable { mutableIntStateOf(0) }
    val icon = if (showDropdownMenu) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown

    OutlinedCard(
        modifier = Modifier
            .clickable { if (devices.size > 1) showDropdownMenu = true }
            .onSizeChanged { width = it.width }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_elevation),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = CircleShape
                        )
                        .padding(8.dp)
                )

                Column {
                    Text(
                        text = stringResource(id = R.string.measured_device),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(text = selectedDevice.address, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.weight(1f))
                if (devices.size > 1) Icon(icon, contentDescription = "")
            }

        }

        DropdownMenu(
            expanded = showDropdownMenu,
            onDismissRequest = { showDropdownMenu = false },
            modifier = Modifier.width(with(LocalDensity.current) { width.toDp() }),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.devices),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider()

                devices.forEach {
                    Text(
                        text = it.address,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .clickable {
                                onEvent(DFSViewEvent.OnBluetoothDeviceSelected(it))
                                showDropdownMenu = false
                            }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MeasuredDevicesPreview() {
    MeasuredDevices(
        PeripheralBluetoothAddress.TEST,
        devices = listOf(
            PeripheralBluetoothAddress.TEST,
            PeripheralBluetoothAddress.TEST,
            PeripheralBluetoothAddress.TEST
        )
    ) {}
}

@Preview(showBackground = true)
@Composable
private fun SectionBluetoothDeviceComponentPreview() {
    SectionBluetoothDeviceComponent(
        DFSServiceData(), null
    ) {}
}

@Composable
internal fun NotSelectedView(
    devices: List<PeripheralBluetoothAddress>,
    onClick: (PeripheralBluetoothAddress) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.devices),
            style = MaterialTheme.typography.titleLarge
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            devices.forEach { address ->
                BluetoothDeviceView(
                    device = address,
                    title = stringResource(id = R.string.device_address),
                    modifier = Modifier.fillMaxWidth()
                ) { onClick(address) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotSelectedViewPreview() {
    NotSelectedView(
        listOf(
            PeripheralBluetoothAddress.TEST,
            PeripheralBluetoothAddress.TEST,
            PeripheralBluetoothAddress.TEST
        )
    ) { }
}

@Preview(showBackground = true)
@Composable
private fun EmptyItemPreview() {
    EmptyView(R.string.device_no_devices, R.string.device_no_devices_hint)
}

@Composable
internal fun BluetoothDeviceView(
    device: PeripheralBluetoothAddress,
    title: String,
    modifier: Modifier = Modifier,
    onClick: (PeripheralBluetoothAddress) -> Unit
) {
    Row(
        modifier = Modifier.clickable { onClick(device) },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_elevation),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = CircleShape
                )
                .padding(8.dp)
        )

        Column(modifier) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = device.address, style = MaterialTheme.typography.bodyMedium)
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun BluetoothDeviceViewPreview() {
    BluetoothDeviceView(
        PeripheralBluetoothAddress.TEST,
        "Bluetooth Device - Test"
    ) {}
}
