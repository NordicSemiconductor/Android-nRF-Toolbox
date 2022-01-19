package no.nordicsemi.android.service

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import javax.inject.Inject

const val DEVICE_DATA = "device-data"

class ServiceManager @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    fun <T> startService(service: Class<T>, device: DiscoveredBluetoothDevice) {
        val intent = Intent(context, service).apply {
            putExtra(DEVICE_DATA, device)
        }
        context.startService(intent)
    }
}
