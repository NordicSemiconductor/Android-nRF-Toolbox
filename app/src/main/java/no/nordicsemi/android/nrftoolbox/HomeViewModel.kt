package no.nordicsemi.android.nrftoolbox

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.service.SelectedBluetoothDeviceHolder
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val deviceHolder: SelectedBluetoothDeviceHolder
) : ViewModel() {

    fun onDeviceSelected(device: DiscoveredBluetoothDevice) {
        deviceHolder.attachDevice(device.device)
    }
}
