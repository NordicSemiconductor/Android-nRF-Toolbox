package no.nordicsemi.android.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.events.exhaustive

@Composable
internal fun ListOfDevicesScreen(onDeviceSelected: (BluetoothDevice) -> Unit) {

    val viewModel = hiltViewModel<NordicBleScannerViewModel>()

    val result = viewModel.scannerResult.collectAsState().value

    when (result) {
        is DeviceListResult -> DeviceListView(result.devices, onDeviceSelected)
        is ScanningErrorResult -> ScanningErrorView()
    }.exhaustive
}

@SuppressLint("MissingPermission")
@Composable
private fun DeviceListView(
    devices: List<BluetoothDevice>,
    onDeviceSelected: (BluetoothDevice) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(id = R.string.scanner__list_of_devices))
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            itemsIndexed(devices) { _, device ->
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onDeviceSelected(device) }
                ) {
                    Column {
                        Text(device.name ?: stringResource(id = R.string.scanner__no_name))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = device.address)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanningErrorView() {
    Text(text = stringResource(id = R.string.scanner__error))
}
