package no.nordicsemi.android.service

import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SelectedBluetoothDeviceHolder @Inject constructor() {

    var device: DiscoveredBluetoothDevice? = null
        private set

    fun attachDevice(newDevice: DiscoveredBluetoothDevice) {
        device = newDevice
    }

    fun forgetDevice() {
        device = null
    }
}
