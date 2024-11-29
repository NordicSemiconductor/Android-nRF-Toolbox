package no.nordicsemi.android.toolbox.libs.profile.view.directionFinder

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.core.data.DFSServiceData
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DFSViewEvent
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.ui.view.ScreenSection

@Composable
internal fun SectionBluetoothDeviceComponent(
    data: DFSServiceData,
    selectedDevice: PeripheralBluetoothAddress?,
    onEvent: (DeviceConnectionViewEvent) -> Unit
) {
    val showDialog = remember { mutableStateOf(false) }
    val devices = data.data.keys.toList()

    ScreenSection {
        when {
            selectedDevice == null && devices.isNotEmpty() -> NotSelectedView {
                showDialog.value = true
            }

            selectedDevice == null -> EmptyItem()
            else -> {
                BluetoothDeviceView(
                    selectedDevice,
                    stringResource(id = R.string.measured_device),
                    devices.size > 1
                ) {
                    if (devices.size > 1) {
                        showDialog.value = true
                    }
                }
            }
        }
    }

    if (showDialog.value) {
        BluetoothDeviceDialog(devices, { onEvent(DFSViewEvent.OnBluetoothDeviceSelected(it)) }) {
            showDialog.value = false
        }
    }
}

@Composable
internal fun NotSelectedView(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            Icons.Default.Search,
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = CircleShape
                )
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = stringResource(id = R.string.device_not_selected),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotSelectedViewPreview() {
    NotSelectedView { }
}

@Composable
internal fun EmptyItem() {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            Icons.Default.Search,
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = CircleShape
                )
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = stringResource(id = R.string.device_no_devices),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyItemPreview() {
    EmptyItem()
}

@Composable
internal fun BluetoothDeviceView(
    device: PeripheralBluetoothAddress,
    title: String,
    showArrow: Boolean,
    onClick: (PeripheralBluetoothAddress) -> Unit,
) {
    Row(
        modifier = Modifier.clickable { onClick(device) },
        horizontalArrangement = Arrangement.Center,
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

        Spacer(modifier = Modifier.padding(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = device.address, style = MaterialTheme.typography.bodyMedium)
        }

        if (showArrow) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = "")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BluetoothDeviceViewPreview() {
    BluetoothDeviceView(
        PeripheralBluetoothAddress.TEST,
        "Bluetooth Device - Test",
        true
    ) { }
}

@Composable
internal fun BluetoothDeviceDialog(
    devices: List<PeripheralBluetoothAddress>,
    onClick: (PeripheralBluetoothAddress) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .height(300.dp)
                .background(MaterialTheme.colorScheme.background),
            shape = RoundedCornerShape(10.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.devices),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.padding(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                        .verticalScroll(rememberScrollState())
                ) {
                    devices.forEach { address ->
                        BluetoothDeviceView(
                            device = address,
                            title = stringResource(id = R.string.device_address),
                            showArrow = false
                        ) {
                            onClick(it)
                            onDismiss()
                        }
                        Spacer(modifier = Modifier.padding(8.dp))
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text(
                            text = stringResource(id = R.string.cancel),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BluetoothDeviceDialogPreview() {
    BluetoothDeviceDialog(
        devices = listOf(
            PeripheralBluetoothAddress.TEST,
            PeripheralBluetoothAddress.TEST,
            PeripheralBluetoothAddress.TEST
        ),
        onClick = {}
    ) { }
}